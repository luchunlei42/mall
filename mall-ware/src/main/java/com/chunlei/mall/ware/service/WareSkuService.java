package com.chunlei.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.ware.entity.WareSkuEntity;
import com.chunlei.mall.ware.vo.LockStockResultVo;
import com.chunlei.mall.ware.vo.SkusHasStockVo;
import com.chunlei.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:24:43
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkusHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

}

