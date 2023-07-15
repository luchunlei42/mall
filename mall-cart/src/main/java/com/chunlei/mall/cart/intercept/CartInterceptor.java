package com.chunlei.mall.cart.intercept;

import com.chunlei.mall.cart.vo.UserInfoTo;
import com.chunlei.mall.cart.vo.UserLoginVo;
import com.chunlei.mall.common.constant.CartConstant;
import com.chunlei.mall.common.vo.MemberResponseVo;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberResponseVo loginUser = (MemberResponseVo) session.getAttribute("loginUser");
        UserInfoTo to = new UserInfoTo();
        if (loginUser != null){
            to.setUserId(loginUser.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null && cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)){
                    to.setUserKey(cookie.getValue());
                }
            }
        }
        threadLocal.set(to);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
        cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
        response.addCookie(cookie);
    }
}
