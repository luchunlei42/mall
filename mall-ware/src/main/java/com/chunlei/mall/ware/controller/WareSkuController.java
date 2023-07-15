package com.chunlei.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.chunlei.mall.ware.vo.LockStockResultVo;
import com.chunlei.mall.ware.vo.SkusHasStockVo;
import com.chunlei.mall.ware.vo.WareSkuLockVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.chunlei.mall.ware.entity.WareSkuEntity;
import com.chunlei.mall.ware.service.WareSkuService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.R;



/**
 * 商品库存
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:24:43
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try{
            Boolean result = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (RuntimeException e){
            return R.error();
        }
    }

    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds){
        //skuid, stock
        List<SkusHasStockVo> vos =  wareSkuService.getSkusHasStock(skuIds);
        R r = R.ok();
        r.setData(vos);
        return r;
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
