package com.chunlei.mall.product.feign;

import com.chunlei.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-ware")
public interface WareFeignService {
    @PostMapping("ware/waresku/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);
}
