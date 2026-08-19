package com.best.hello.entity;

/**
 * entity 实体类代码
 * @date 2021/07/23
 */

public class User {

    private Integer id;
    private String user;
    private String pass;

    public Integer getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
