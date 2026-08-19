package com.best.hello.controller;

import com.best.hello.util.JwtUtils;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api("JWT")
@RestController
@RequestMapping("/vulnapi/JWT")
public class JWT {
    Logger log = LoggerFactory.getLogger(JWT.class);

    /**
     * 读取Cookie，获取JWT中当前用户
     */
    @GetMapping("/getName")
    public String getNickname(@CookieValue("JWT_TOKEN") String jwt_cookie) {
        String username = JwtUtils.getUsernameByJwt(jwt_cookie);
        log.info("当前JWT用户是：{}", username);
        return "当前JWT用户是：" + username;
    }


}
