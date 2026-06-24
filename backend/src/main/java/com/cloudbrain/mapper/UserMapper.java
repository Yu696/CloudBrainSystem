package com.cloudbrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbrain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 绕过逻辑删除查用户名是否存在 */
    @Select("SELECT COUNT(*) FROM `user` WHERE username = #{username}")
    long countByUsername(String username);
}
