package com.chunlei.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 21:39:13
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

