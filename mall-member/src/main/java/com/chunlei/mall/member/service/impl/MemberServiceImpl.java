package com.chunlei.mall.member.service.impl;

import com.chunlei.mall.member.dao.MemberLevelDao;
import com.chunlei.mall.member.entity.MemberLevelEntity;
import com.chunlei.mall.member.vo.MemberLoginVo;
import com.chunlei.mall.member.vo.MemberRegisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.member.dao.MemberDao;
import com.chunlei.mall.member.entity.MemberEntity;
import com.chunlei.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao levelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisVo vo){
        MemberEntity member = new MemberEntity();
        //设置默认等级
        MemberLevelEntity levelEntity = levelDao.getDefaultLevel();
        member.setLevelId(levelEntity.getId());
        member.setUsername(vo.getUsername());
        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());
        //密码要加密保存
        this.baseMapper.insert(member);
    }

    @Override
    public void checkPhoneUnique(String phone) throws RuntimeException{
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0){
            throw new RuntimeException("手机号已存在");
        }

    }

    @Override
    public void checkUsernameUnique(String username) throws RuntimeException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0){
            throw new RuntimeException("用户名已存在");
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1.去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (entity == null){
            return null;
        }else {
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches){
                return entity;
            }else{
                return null;
            }
        }
    }

}