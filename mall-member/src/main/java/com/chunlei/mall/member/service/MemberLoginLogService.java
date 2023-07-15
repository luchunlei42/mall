package com.chunlei.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.member.entity.MemberLoginLogEntity;

import java.util.Map;

/**
 * 会员登录记录
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:16:37
 */
public interface MemberLoginLogService extends IService<MemberLoginLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

