package com.chunlei.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.member.entity.MemberEntity;
import com.chunlei.mall.member.vo.MemberLoginVo;
import com.chunlei.mall.member.vo.MemberRegisVo;

import java.util.Map;

/**
 * 会员
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:16:37
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisVo vo);

    void checkPhoneUnique(String phone) throws RuntimeException;

    void checkUsernameUnique(String username) throws RuntimeException;

    MemberEntity login(MemberLoginVo vo);
}

