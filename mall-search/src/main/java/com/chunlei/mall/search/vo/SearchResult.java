package com.chunlei.mall.search.vo;

import com.chunlei.mall.common.to.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    private List<SkuEsModel> products;
    /**
     * 分页信息
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPage;

    private List<BrandVO> brands;
    private List<AttrVo> attrs;
    private List<CatalogVo> catalogs;


    @Data
    public static class BrandVO{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
