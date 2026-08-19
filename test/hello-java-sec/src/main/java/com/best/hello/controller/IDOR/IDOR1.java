package com.best.hello.controller.IDOR;

import com.alibaba.fastjson.JSON;
import com.best.hello.entity.User;
import com.best.hello.entity.UserIDOR;
import com.best.hello.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("水平越权")
@Slf4j
@RestController
@RequestMapping("/vulnapi/IDOR")
public class IDOR1 {

    @Autowired
    private UserMapper userMapper;

    // http://127.0.0.1:8888/IDOR/vul/info?name=admin
    @ApiOperation(value = "vul：根据name查询用户信息")
    @GetMapping("/vul/info")
    public List<UserIDOR> vul(String name) {
        log.info("[vul] 基于用户身份越权查询：{}", name);
        return userMapper.queryByUser2(name);
    }

    @ApiOperation(value = "vul：基于对象ID越权查询")
    @GetMapping(value = "/vul/userid")
    public List<User> vul2(Integer id) {
        log.info("[vul] 基于对象ID越权查询：{}", id);
        return userMapper.queryByIdAsInterger(id);
    }

    @ApiOperation(value = "safe：只允许查询自己信息")
    @GetMapping("/safe/info")
    public Object safe(String name, HttpSession session) {
        log.info("[safe] 水平越权查询：{}", name);
        String loginUser = (String) session.getAttribute("LoginUser");
        Map<String, String> m = new HashMap<>();

        // 判断当前session与要查询的用户是否一致
        if (loginUser.equals(name)) {
            m.put("user", loginUser);
        } else {
            m.put("info", "无权限查询其他用户！");
        }
        return JSON.toJSONString(m);
    }

    @ApiOperation(value = "safe：通过UUID查询用户信息")
    @GetMapping("/safe/userid")
    public List<User> safe2(@RequestParam("uuid") String uuid) {
        log.info("[safe] 查询UUID {}", uuid);
        return userMapper.queryByUuid(uuid);
    }

}
