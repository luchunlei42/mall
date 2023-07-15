package com.chunlei.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.chunlei.mall.auth.feign.MemberFeignService;
import com.chunlei.mall.auth.utils.RedisUtils;
import com.chunlei.mall.auth.vo.UserLoginVo;
import com.chunlei.mall.auth.vo.UserRegisterVo;
import com.chunlei.mall.common.utils.R;
import com.chunlei.mall.common.vo.MemberResponseVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage(){
        return "reg";
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRegisterVo vo, BindingResult result, Model model){
        if (result.hasErrors()){
            Map<String, String> errors = new HashMap<>();
            errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
            model.addAttribute("errors", errors);
            return "redirect:http://auth.mall.com/reg.html";
        }

        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(RedisUtils.RedisSMSCodePrefix + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode)){
            if (code.equals(redisCode.split("_")[0])){
                redisTemplate.delete(RedisUtils.RedisSMSCodePrefix + vo.getPhone());
                R register = memberFeignService.register(vo);
                if (register.getCode() == 0){
                    //成功
                    return "redirect:http://auth.mall.com/login.html";
                }else {
                    return "redirect:http://auth.mall.com/reg.html";
                }
            }
        }
        return "redirect:http://auth.mall.com/reg.html";
    }

    @PostMapping("/login")
    public String login(@RequestBody UserLoginVo vo, HttpSession session){

        R login = memberFeignService.login(vo);
        if (login.getCode() == 0){
            session.setAttribute("loginUser", login.getData(new TypeReference<MemberResponseVo>(){}));
            return "redirect:http://mall.com";
        }else {
            return "redirect:http://auth.mall.com/login.html";
        }

    }
}
