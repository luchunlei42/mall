package com.chunlei.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.common.to.OrderTo;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.common.vo.mq.StockDetailTo;
import com.chunlei.mall.common.vo.mq.StockLockedTo;
import com.chunlei.mall.ware.entity.WareOrderTaskDetailEntity;
import com.chunlei.mall.ware.entity.WareOrderTaskEntity;
import com.chunlei.mall.ware.feign.OrderFeignService;
import com.chunlei.mall.ware.feign.ProductFeignService;
import com.chunlei.mall.ware.service.WareOrderTaskDetailService;
import com.chunlei.mall.ware.service.WareOrderTaskService;
import com.chunlei.mall.ware.vo.*;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.ware.dao.WareSkuDao;
import com.chunlei.mall.ware.entity.WareSkuEntity;
import com.chunlei.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 库存自动解锁
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        Long id = to.getId();
        StockDetailTo detailTo = to.getDetailTo();
        Long skuId = detailTo.getSkuId();
        Long detailId = detailTo.getId();
        //解锁
        WareOrderTaskDetailEntity orderTaskDetail = orderTaskDetailService.getById(detailId);
        if (orderTaskDetail!=null){
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0){
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });
                if (orderVo == null || orderVo.getStatus() == 4){
                    //订单被取消了
                    unLockStock(skuId, detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }else {
                //失败，继续消费
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        }else {
            //无需解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo order, Message message, Channel channel) throws IOException {
        try {
            unLockStock(order);
            String orderSn = order.getOrderSn();
            WareOrderTaskEntity task = orderTaskService.getOrderTaskBuOrderSn(order);
            Long id = task.getId();
            List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id)
                    .eq("lock_status", 1));
            for (WareOrderTaskDetailEntity entity : list) {
                unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
            }
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);

        }catch (Exception e){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }
    }

    private void unLockStock(OrderTo order) {

    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId){
        wareSkuDao.unlockStock(skuId,wareId,num);
    }
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String)params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            queryWrapper.and(w->{
                w.eq("sku_id",skuId);
            });
        }
        String wareId = (String)params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            queryWrapper.and(w->{
                w.eq("ware_id",wareId);
            });
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1,判断记录是否有，没有新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size()==0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setWareId(wareId);
            skuEntity.setStock(skuNum);
            skuEntity.setStockLocked(0);
            //远程查询sku的名字
            try {
                R info = productFeignService.info(skuId);
                Map<String , Object> data = (Map<String, Object>) info.get("skuInfo");
                if (data != null && !StringUtils.isEmpty((String) data.get("skuName"))){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e) {
            }
            wareSkuDao.insert(skuEntity);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkusHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkusHasStockVo> vos = skuIds.stream().map(skuId -> {
            SkusHasStockVo vo = new SkusHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count==null?false:count>0);
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();



        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;

        }).collect(Collectors.toList());
        Boolean allLock = true;
        for (SkuWareHasStock stock : collect) {
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() ==0){
                throw new RuntimeException(skuId.toString());
            }
            Boolean skuStocked = false;
            for (Long wareId : wareIds) {
                //成功返回一
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,stock.getNum());
                if (count == 1){
                    //当前仓库失败，所一个
                    skuStocked = true;
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null,skuId,"", stock.getNum(), wareOrderTaskEntity.getId(), wareId,null);
                    orderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    lockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked", lockedTo);

                    break;
                }else {

                }
            }
            if (!skuStocked){
                throw new RuntimeException(skuId.toString());
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private List<Long> wareId;
        private Integer num;
    }



}