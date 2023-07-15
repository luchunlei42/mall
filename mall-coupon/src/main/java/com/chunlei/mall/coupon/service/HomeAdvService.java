package com.chunlei.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.coupon.entity.HomeAdvEntity;

import java.util.Map;

/**
 * 首页轮播广告
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:11:27
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

