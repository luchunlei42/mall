package com.chunlei.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.ware.feign.MemberFeignService;
import com.chunlei.mall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chunlei.mall.common.utils.PageUtils;
import com.chunlei.mall.common.utils.Query;

import com.chunlei.mall.ware.dao.WareInfoDao;
import com.chunlei.mall.ware.entity.WareInfoEntity;
import com.chunlei.mall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and(w->{
                w.eq("id",key).or().like("name",key)
                        .or().like("address", key)
                        .or().like("areacode", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addrId) {
        R r = memberFeignService.info(addrId);
        MemberAddressVo data = r.getData(new TypeReference<MemberAddressVo>() {
        });
        if (data!=null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1);
            return new BigDecimal(substring);
        }
        return null;
    }

}