package com.chunlei.mall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 18, message = "用户名长度不对")
    private String username;
    @NotEmpty
    @Length(min = 6,max = 18, message = "密码必须6-18位")
    private String password;
    private String code;
    private String phone;
}
