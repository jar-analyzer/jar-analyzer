package com.example.test.controller;

import com.example.test.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST风格API控制器
 * 演示RESTful API的各种HTTP方法
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    // 模拟用户数据库
    private static final Map<Long, User> USERS = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    // 初始化一些测试数据
    static {
        User user1 = new User(ID_GENERATOR.getAndIncrement(), "张三", "zhangsan@example.com", 25);
        User user2 = new User(ID_GENERATOR.getAndIncrement(), "李四", "lisi@example.com", 30);
        User user3 = new User(ID_GENERATOR.getAndIncrement(), "王五", "wangwu@example.com", 28);

        USERS.put(user1.getId(), user1);
        USERS.put(user2.getId(), user2);
        USERS.put(user3.getId(), user3);
    }

    /**
     * 获取所有用户
     * GET /api/users
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return new ArrayList<>(USERS.values());
    }

    /**
     * 获取单个用户
     * GET /api/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = USERS.get(id);
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "User not found with id: " + id);
            error.put("status", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 创建用户
     * POST /api/users
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setId(ID_GENERATOR.getAndIncrement());
        USERS.put(user.getId(), user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * 更新用户
     * PUT /api/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!USERS.containsKey(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "User not found with id: " + id);
            error.put("status", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        user.setId(id);
        USERS.put(id, user);
        return ResponseEntity.ok(user);
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!USERS.containsKey(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "User not found with id: " + id);
            error.put("status", HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        User deletedUser = USERS.remove(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("deletedUser", deletedUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索用户
     * GET /api/users/search?name=xxx
     */
    @GetMapping("/users/search")
    public List<User> searchUsers(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer age) {
        List<User> result = new ArrayList<>();

        for (User user : USERS.values()) {
            boolean nameMatch = name == null || user.getName().contains(name);
            boolean ageMatch = age == null || user.getAge().equals(age);

            if (nameMatch && ageMatch) {
                result.add(user);
            }
        }

        return result;
    }
}