package com.best.hello.controller.SQLI;

import com.best.hello.entity.User;
import com.best.hello.mapper.UserMapper;
import com.best.hello.util.Security;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("SQL注入 - MyBatis")
@RestController
@RequestMapping("/vulnapi/sqli/mybatis")
public class MyBatis {
    Logger log = LoggerFactory.getLogger(MyBatis.class);

    @Autowired
    private UserMapper userMapper;

    @ApiOperation(value = "vul：Mybatis使用${}查询字符串型ID")
    @GetMapping("/vul/id/{id}")
    public List<User> queryById(@PathVariable String id) {
        return userMapper.queryByIdAsString(id);
    }

    @ApiOperation(value = "vul：Mybatis使用${}模糊查询")
    @GetMapping("/vul/search")
    public List<User> searchVul(@RequestParam("user") String user) {
        log.info("[vul] MyBatis 查询参数: {}", user);
        return userMapper.searchVul(user);
    }

    /**
     * @param field 排序字段
     * @param sort  排序方式
     * @return 按指定排序方式排序的用户列表
     */
    @ApiOperation(value = "vul：Mybatis order by 注入（xml方式）", notes = "#{} 会将对象转成字符串，形成 order by \"user\" desc 造成错误(无法正常排序)，因此很多研发会采用${}来解决，从而造成SQL注入")
    @GetMapping("/vul/order")
    public List<User> orderBy(String field, String sort) {
        log.info("[vul] mybaits: order by " + field + " " + sort);
        return userMapper.orderBy(field, sort);
    }

    @ApiOperation(value = "vul：Mybatis order by 注入（注解方式）")
    @GetMapping("/vul/order2")
    public List<User> orderBy2(String field, String sort) {
        log.info("[vul] mybaits: order by " + field + " " + sort);
        return userMapper.orderBy2(field, sort);
    }


    // -----------------------------------安全分割线-----------------------------------

    @ApiOperation(value = "safe：Mybatis使用 #{}")
    @GetMapping("/safe/search")
    public List<User> searchSafe(String user) {
        return userMapper.searchSafe(user);
    }

    @ApiOperation(value = "safe：Mybatis 查询所有用户")
    @GetMapping("/safe/list")
    public List<User> list() {
        return userMapper.list();
    }

    @ApiOperation(value = "safe：Mybatis使用${}，但写死Int类型，无法注入")
    @GetMapping("/safe/id/{id}")
    public List<User> queryById(@PathVariable Integer id) {
        log.info("[safe] mybaits: {}", id);
        return userMapper.queryByIdAsInterger(id);
    }

    @ApiOperation(value = "safe：Mybatis order by 做映射")
    @GetMapping("/safe/order")
    public List<User> orderBySafe(String field) {
        return userMapper.orderBySafe(field);
    }

    @ApiOperation(value = "safe: MyBatis Order By 白名单与正则验证")
    @GetMapping("/safe/orderbyre")
    public List<User> orderBySafeRe(String field, String sort) {
        if (Security.isValidOrder(field) && Security.isValidSort(sort)) {
            log.info("[safe] 验证通过: order by {} {}", field, sort);
            return userMapper.orderBy2(field, sort);
        } else {
            // 如果参数不合法，则返回默认排序
            log.warn("[safe] 无效的排序参数：order by {} {}", field, sort);
            return userMapper.orderBy2("id", "desc");
        }
    }

}
