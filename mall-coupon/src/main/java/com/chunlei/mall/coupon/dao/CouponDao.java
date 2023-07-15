package com.chunlei.mall.coupon.dao;

import com.chunlei.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:11:27
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
