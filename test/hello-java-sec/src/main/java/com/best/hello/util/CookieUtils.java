package com.best.hello.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Slf4j
public class CookieUtils {

    public static final int COOKIE_MAX_AGE = 7 * 24 * 3600;
    public static final int COOKIE_HALF_HOUR = 30 * 60;

    /**
     * 查找指定名称的Cookie对象，不存在该对象则返回null
     */
    public static Cookie findCookie(String name, Cookie[] cookies) {
        if(name == null || cookies == null || cookies.length == 0) {
            return null;
        }
        for(Cookie cookie : cookies) {
            if(name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }


    public static boolean deleteCookie(HttpServletResponse response, String cookieName) {
        try {
            // 要删除Cookie，需要将Max-Age设置为0，并且将Cookie的值设置为null
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }
}