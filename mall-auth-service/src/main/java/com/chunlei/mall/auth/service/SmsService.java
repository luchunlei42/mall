package com.chunlei.mall.auth.service;

import com.chunlei.mall.auth.utils.RedisUtils;
import com.chunlei.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsService {
    @Autowired
    StringRedisTemplate redisTemplate;
    public R sendCode(String phone){

        //1.接口防刷
        String redisCode = redisTemplate.opsForValue().get(RedisUtils.RedisSMSCodePrefix + phone);
        if (!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000){
                return R.error("验证码间隔太短");
            }
        }
        //2，验证码校验
        String code = randomCode();
        String codeAndTime = code +"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(RedisUtils.RedisSMSCodePrefix+phone, codeAndTime, RedisUtils.RedisSMSCodeExpireTime, TimeUnit.MINUTES);
        log.info("发送验证码：{}",code);
        return R.ok();
    }

    private String randomCode(){
        //定义取值范围
        String str = "0123456789";
        //容量为4
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            //遍历4次，拿到某个字符并且拼接
            char ch = str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        return sb.toString();
    }
}
