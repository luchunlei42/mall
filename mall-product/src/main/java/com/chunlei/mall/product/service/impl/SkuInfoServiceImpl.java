package com.chunlei.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.product.config.ThreadConfig;
import com.chunlei.mall.product.entity.SkuImagesEntity;
import com.chunlei.mall.product.entity.SpuInfoDescEntity;
import com.chunlei.mall.product.feign.SeckillFeignService;
import com.chunlei.mall.product.service.*;
import com.chunlei.mall.product.vo.SeckillSkuVo;
import com.chunlei.mall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.product.dao.SkuInfoDao;
import com.chunlei.mall.product.entity.SkuInfoEntity;

import javax.xml.ws.soap.Addressing;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    SeckillFeignService seckillFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(w-> w.eq("sku_id",key).or().like("sku_name",key));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            queryWrapper.and(w-> w.eq("catalog_id",catelogId));
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(catelogId)){
            queryWrapper.and(w-> w.eq("brand_id",brandId));
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)){
            queryWrapper.and(w-> w.ge("price",min));
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.and(w-> w.le("price",max));
                }
            }catch (Exception e){

            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySkuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return  list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = new SkuItemVo();


        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = getById(skuId);
            vo.setInfo(skuInfo);
            return skuInfo;
        }, executor);

        infoFuture.thenAcceptAsync((res) -> {
            //spu销售属性组合
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVoList = saleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            vo.setSaleAttrs(saleAttrVoList);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //spu 介绍
            Long spuId = res.getSpuId();
            SpuInfoDescEntity descEntity = spuInfoDescService.getById(spuId);
            vo.setDesp(descEntity);
        }, executor);

        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SpuItemAttrGroupVo> attrGroupVoList = attrGroupService.getAttrGroupWithAttrsBuSpuId(res.getSpuId(), res.getCatalogId());
            vo.setGroupAttrs(attrGroupVoList);
        },executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            vo.setImages(images);
        },executor);

        //查询秒杀优惠
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.skuSeckillInfo(skuId);
            if (r.getCode()==0){
                SeckillSkuVo seckillSkuVo = r.getData(new TypeReference<SeckillSkuVo>() {
                });
                vo.setSeckillInfo(seckillSkuVo);
            }
        },executor);


        //等待所有vo都完成
        CompletableFuture.allOf(attrGroupFuture,imageFuture,descFuture,seckillFuture).get();

        return vo;
    }

}