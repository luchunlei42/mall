package com.chunlei.mall.seckill.service;

import com.chunlei.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSku();

    SeckillSkuRedisTo skuSeckillInfo(Long skuId);

    String secKill(String killId, String key, Integer num);
}
