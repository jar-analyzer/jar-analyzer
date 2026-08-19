package com.best.hello.mapper;

import com.best.hello.entity.User;
import com.best.hello.entity.UserIDOR;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 传统的xml配置
     */
    List<User> orderBy(String field, String sort);

    List<User> orderBySafe(String field);


    /**
     * MyBatis3 提供了新的基于注解的配置，通过注解不在需要配置繁杂的xml文件
     */
    @Select("select * from users where user like CONCAT('%', #{user}, '%')")
    List<User> searchSafe(@Param("user") String user);

    // like搜索时使用 '%#{q}%' 会报错，因此容易写成 ${}
    @Select("select * from users where user like '%${user}%'")
    List<User> searchVul(String user);

    @Select("select * from users where user = #{user}")
    List<UserIDOR> queryByUser2(@Param("user") String user);

    @Select("select * from users where id = ${id}")
    List<User> queryByIdAsString(@Param("id") String id);

    @Select("select * from users where id = ${id}")
    List<User> queryByIdAsInterger(@Param("id") Integer id);

    @Select("SELECT * FROM users WHERE uuid = #{uuid}")
    List<User> queryByUuid(@Param("uuid") String uuid);

    // 使用 #{} 会产生报错，因此容易写成 ${}
    @Select("select * from users order by ${field} ${sort}")
    List<User> orderBy2(@Param("field") String field, @Param("sort") String sort);

    @Select("select * from users")
    List<User> list();

}
