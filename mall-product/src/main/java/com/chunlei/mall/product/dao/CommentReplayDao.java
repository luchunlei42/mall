package com.chunlei.mall.product.dao;

import com.chunlei.mall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-04 23:38:13
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
