package com.chunlei.mall.seckill.interceptor;

import com.chunlei.mall.common.constant.AuthServerConstant;
import com.chunlei.mall.common.vo.MemberResponseVo;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", uri);
        if (match){
            MemberResponseVo attribute = (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null){
                loginUser.set(attribute);
                return true;
            }else {
                //没登陆登录
                request.getSession().setAttribute("msg","请先登录");
                response.sendRedirect("http://auth.mall.com/login.com");
                return false;
            }
        }
        return true;
    }
}
