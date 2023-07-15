package com.chunlei.mall.seckill.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.common.vo.MemberResponseVo;
import com.chunlei.mall.common.vo.mq.SeckillOrderTo;
import com.chunlei.mall.seckill.feign.CouponFeignService;
import com.chunlei.mall.seckill.feign.ProductFeignService;
import com.chunlei.mall.seckill.interceptor.LoginUserInterceptor;
import com.chunlei.mall.seckill.service.SeckillService;
import com.chunlei.mall.seckill.to.SeckillSkuRedisTo;
import com.chunlei.mall.seckill.vo.SeckillSessionWithSkusVo;
import com.chunlei.mall.seckill.vo.SeckillSkuVo;
import com.chunlei.mall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock";//+商品随机码


    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描最近3天需要参与上架的商品
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode()==0){
            List<SeckillSessionWithSkusVo> sessions = r.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            saveSessionInfo(sessions);
            saveSessionSkuInfo(sessions);
        }

    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSku() {

        //确定当前时间是那个场次
        long now = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key: keys){
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            if (now>=startTime && now<=endTime){
                //获取商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<Object> list = ops.multiGet(range);
                if (list!=null){
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                        //redisTo.setRandomCode(null);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo skuSeckillInfo(Long skuId) {

        //找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String , String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys!=null&&keys.size()>0){
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                boolean matches = Pattern.matches(regx, key);
                if (matches){
                    String s = ops.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);

                    //随机码
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    long now = new Date().getTime();
                    if (now>=startTime&&now<=endTime){
                    }else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }

            }
        }
        return null;
    }

    @Override
    public String secKill(String killId, String key, Integer num) {

        MemberResponseVo respVo = LoginUserInterceptor.loginUser.get();

        //获取商品详情信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = ops.get(killId);
        if (StringUtils.isEmpty(s)){
            return null;
        }else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            //校验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();
            if (time >= startTime && time<= endTime){
                long ttl  =endTime-time;
                //校验随机码
                String randomCode = redisTo.getRandomCode();
                String id = redisTo.getPromotionSessionId()+"_"+redisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(id)){
                    //验证购物数量上是否合理
                    Integer seckillLimit = redisTo.getSeckillLimit();
                    if (seckillLimit >= num){
                        //验证是否买过了
                        String redisKey = respVo.getId()+"_"+id;
                        //过期时间当前场次
                        Boolean absent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (absent){
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE);

                            boolean b = semaphore.tryAcquire(num);
                            if (!b){
                                return null;
                            }
                            String timeId = IdWorker.getTimeId();

                            SeckillOrderTo order = new SeckillOrderTo();
                            order.setOrderSn(timeId);
                            order.setSkuId(redisTo.getSkuId());
                            order.setMemberId(respVo.getId());
                            order.setNum(num);
                            order.setPromotionSessionId(redisTo.getPromotionSessionId());
                            order.setSeckillPrice(redisTo.getSeckillPrice());

                            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",order);
                            return timeId;
                            }
                        }else {
                            return null;
                        }
                    }
                }else {
                    return null;
                }
            }
        return null;
    }

    private void saveSessionInfo(List<SeckillSessionWithSkusVo> sessions){
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX+startTime+"_"+endTime;
            List<String> collect = session.getRelationSkus().stream().map(item->item.getPromotionSessionId()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey){
                redisTemplate.opsForList().leftPushAll(key,collect);
            }
        });
    }

    private void saveSessionSkuInfo(List<SeckillSessionWithSkusVo> sessions){


        //准备hash操作
        sessions.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId()+"_"+seckillSkuVo.getId())){
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (r.getCode()==0){
                        SkuInfoVo info = r.getData(new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(info);
                    }
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //随机码
                    redisTo.setRandomCode(token);
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);
                    String jsonString = JSON.toJSONString(redisTo);
                    redisTemplate.opsForHash().put(SKUKILL_CACHE_PREFIX+session.getId(),seckillSkuVo.getPromotionSessionId()+"_"+seckillSkuVo.getId(),jsonString);
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
