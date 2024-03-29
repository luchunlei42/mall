package com.chunlei.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.chunlei.mall.member.vo.MemberLoginVo;
import com.chunlei.mall.member.vo.MemberRegisVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.chunlei.mall.member.entity.MemberEntity;
import com.chunlei.mall.member.service.MemberService;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.R;



/**
 * 会员
 *
 * @author chunlei
 * @email luchunlei42@gmail.com
 * @date 2023-06-05 00:16:37
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){

        MemberEntity entity = memberService.login(vo);

        if (entity != null){
            MemberRegisVo memberRegisVo = new MemberRegisVo();
            BeanUtils.copyProperties(entity, memberRegisVo);
            R r = R.ok();
            r.setData(memberRegisVo);
            return r;
        }else {
            return R.error();
        }
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisVo vo){
        try {
            memberService.register(vo);
        }catch (RuntimeException e){
            return R.error();
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
