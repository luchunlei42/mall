package com.chunlei.mall.product.dao;

import com.chunlei.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:02:21
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
