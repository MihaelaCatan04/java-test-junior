package com.java.test.junior.service;

import com.java.test.junior.model.UserDTO;
import org.apache.ibatis.annotations.Mapper;


public interface UserService {
    UserDTO findByUsername(String username);
    void save(UserDTO userDTO);
}
