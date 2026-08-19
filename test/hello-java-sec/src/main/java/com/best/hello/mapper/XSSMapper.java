package com.best.hello.mapper;

import com.best.hello.entity.XSSEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface XSSMapper {

    @Select("select * from xss order by id desc")
    List<XSSEntity> list();


    @Delete("delete from xss where id = #{id}")
    Integer deleteFeedById(Integer id);

    @Insert("INSERT INTO xss(user, content, date) values(#{user}, #{content}, #{date}) ")
    Integer add(String user, String content, String date);
}
