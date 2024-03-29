package com.chunlei.mall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    private String keyword;
    private Long catalog3Id;
    /**
     * saleCount_asc/desc
     * skuPrice
     * hotScore
     */
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum = 1;
}
