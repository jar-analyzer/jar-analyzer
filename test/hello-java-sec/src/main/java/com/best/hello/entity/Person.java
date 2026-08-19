package com.best.hello.entity;

/**
 * 实体类 - 测试用
 */
public class Person {

    private Integer id;
    private String name;
    private Integer age;

    public Person() {
        System.out.println("无参数构造函数被调用");
    }

    public Person(Integer id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
        System.out.println("有参数构造函数被调用");
    }

    // 创建setter和getter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        System.out.println("getName方法被调用");
        return name;
    }

    public void setName(String name) {
        System.out.println("setName方法被调用");
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
