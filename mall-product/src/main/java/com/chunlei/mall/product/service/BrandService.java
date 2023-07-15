package com.chunlei.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:02:21
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

