package com.java.test.junior.mapper;

import com.java.test.junior.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    void save(User user);
}
