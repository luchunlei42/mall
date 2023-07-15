package com.chunlei.mall.order.feign;

import com.chunlei.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
