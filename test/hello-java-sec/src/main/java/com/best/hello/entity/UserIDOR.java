package com.best.hello.entity;

/**
 * entity 实体类代码 - SQLinjection and IDOR
 * @date 2021/07/23
 */

public class UserIDOR {

    private Integer id;
    private String user;
    private String pass;
    private String flag;

    public Integer getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getFlag() {
        return flag;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
