package com.java.test.junior.service;

import com.java.test.junior.exception.UserAlreadyExistsException;
import com.java.test.junior.exception.UserNotFoundException;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.User;
import com.java.test.junior.model.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Override
    public UserDTO findByUsername(String username) {
        User user =  userMapper.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        };

        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        return dto;
    }

    @Override
    public void save(UserDTO userDTO) {
        try {
            findByUsername(userDTO.getUsername());
            throw new UserAlreadyExistsException("User already exists!");
        } catch (UserNotFoundException e) {
            User userEntity = new User();
            userEntity.setUsername(userDTO.getUsername());
            userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userEntity.setRole("USER");
            userMapper.save(userEntity);
        }
    }
}
