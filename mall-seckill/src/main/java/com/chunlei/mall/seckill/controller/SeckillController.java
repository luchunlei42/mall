package com.chunlei.mall.seckill.controller;

import com.alibaba.druid.mock.MockArray;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.seckill.service.SeckillService;
import com.chunlei.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {
    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSku(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSku();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R skuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.skuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model){
        //判断是否登录
        String orderSn = seckillService.secKill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
