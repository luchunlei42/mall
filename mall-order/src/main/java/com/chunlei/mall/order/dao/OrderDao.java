package com.chunlei.mall.order.dao;

import com.chunlei.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 21:39:13
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
