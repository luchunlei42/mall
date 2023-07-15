package com.chunlei.mall.auth.controller;

import com.chunlei.mall.auth.service.SmsService;
import com.chunlei.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmsSendController {
    @Autowired
    SmsService smsService;

    @GetMapping("/sms")
    public R sendCode(@RequestParam("phone") String phone){
        if (StringUtils.isEmpty(phone)){
            return R.error();
        }
        return smsService.sendCode(phone);

    }
}
