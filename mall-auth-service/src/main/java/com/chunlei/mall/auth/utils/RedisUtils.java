package com.chunlei.mall.auth.utils;

import org.springframework.stereotype.Component;

public class RedisUtils {

    public static final String RedisSMSCodePrefix = "sms:code:";

    public static final Long RedisSMSCodeExpireTime = 10L;
}
