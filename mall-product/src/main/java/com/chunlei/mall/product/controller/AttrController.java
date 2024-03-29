package com.chunlei.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.chunlei.mall.product.entity.AttrEntity;
import com.chunlei.mall.product.entity.ProductAttrValueEntity;
import com.chunlei.mall.product.service.ProductAttrValueService;
import com.chunlei.mall.product.vo.AttrGroupRelationVo;
import com.chunlei.mall.product.vo.AttrRespVo;
import com.chunlei.mall.product.vo.AttrVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.chunlei.mall.product.service.AttrService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.R;



/**
 * 商品属性
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:03:51
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrList(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data",entities);
    }

    @RequestMapping("/info/{attrId}")
    @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId")Long attrId){
        AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    @RequestMapping("/update/{spuId}")
    @RequiresPermissions("product:attr:update")
    public R update(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type){
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }

}
