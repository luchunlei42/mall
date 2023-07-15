package com.chunlei.mall.product.dao;

import com.chunlei.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chunlei.mall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性分组
 * 
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:03:51
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBuSpuId(Long spuId, Long catalogId);
}
