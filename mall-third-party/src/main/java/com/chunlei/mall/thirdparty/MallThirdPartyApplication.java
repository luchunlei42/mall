package com.chunlei.mall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class MallThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallThirdPartyApplication.class,args);
    }
}
