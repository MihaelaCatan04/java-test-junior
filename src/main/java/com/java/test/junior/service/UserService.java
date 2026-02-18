package com.java.test.junior.service;

import com.java.test.junior.model.User;
import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.model.UserResponseDTO;


public interface UserService {
    UserResponseDTO findByUsername(String username);

    UserResponseDTO save(UserRegistrationDTO userRegistrationDTO, String username);

    String getUsernameById(Long id);

    User getUserByUsername(String username);

    User getUserByRole(String role);
}
