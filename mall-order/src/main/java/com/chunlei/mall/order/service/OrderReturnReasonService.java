package com.chunlei.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * 退货原因
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 21:39:13
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

