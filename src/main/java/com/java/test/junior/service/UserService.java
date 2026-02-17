package com.java.test.junior.service;

import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.model.UserResponseDTO;


public interface UserService {
    UserResponseDTO findByUsername(String username);
    UserResponseDTO save(UserRegistrationDTO userRegistrationDTO, String username);
}
