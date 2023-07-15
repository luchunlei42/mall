package com.chunlei.mall.product.vo;

import com.chunlei.mall.product.entity.SkuImagesEntity;
import com.chunlei.mall.product.entity.SkuInfoEntity;
import com.chunlei.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    private SkuInfoEntity info;
    private List<SkuImagesEntity> images;
    private SpuInfoDescEntity desp;
    List<SpuItemAttrGroupVo> groupAttrs;
    List<SkuItemSaleAttrVo> saleAttrs;
    SeckillSkuVo seckillInfo;
    //spu 销售属性组合
    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}
