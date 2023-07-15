package com.chunlei.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.to.OrderTo;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:24:43
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskBuOrderSn(OrderTo order);
}

