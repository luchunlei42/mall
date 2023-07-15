package com.chunlei.mall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName(".mall.com");
        defaultCookieSerializer.setCookieName("MALLSESSION");
        return defaultCookieSerializer;

    }

    @Bean
    public RedisSerializer<Object> springSessionDefaulltRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
