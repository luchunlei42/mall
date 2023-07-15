package com.chunlei.mall.coupon.service.impl;

import com.chunlei.mall.common.to.MemberPrice;
import com.chunlei.mall.common.to.SkuReductionTo;
import com.chunlei.mall.coupon.entity.MemberPriceEntity;
import com.chunlei.mall.coupon.entity.SkuLadderEntity;
import com.chunlei.mall.coupon.service.MemberPriceService;
import com.chunlei.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.coupon.dao.SkuFullReductionDao;
import com.chunlei.mall.coupon.entity.SkuFullReductionEntity;
import com.chunlei.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {


    @Autowired
    SkuLadderService ladderService;

    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1.保存满减打折，会员价
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuReductionTo.getFullCount()>0){
            ladderService.save(skuLadderEntity);
        }
        //2.full_reduction
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, fullReductionEntity);
        if (fullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1) {
            this.save(fullReductionEntity);
        }

        //3.sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> prices = memberPrice.stream().map(item -> {
            MemberPriceEntity price = new MemberPriceEntity();
            price.setSkuId(skuReductionTo.getSkuId());
            price.setMemberLevelId(item.getId());
            price.setMemberLevelName(item.getName());
            price.setMemberPrice(item.getPrice());
            price.setAddOther(1);
            return price;
        }).filter(item-> item.getMemberPrice().compareTo(new BigDecimal("0")) == 1).collect(Collectors.toList());
        memberPriceService.saveBatch(prices);
    }

}