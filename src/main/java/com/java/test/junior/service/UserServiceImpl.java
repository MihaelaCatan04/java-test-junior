package com.java.test.junior.service;

import com.java.test.junior.exception.UserAlreadyExistsException;
import com.java.test.junior.exception.UserNotFoundException;
import com.java.test.junior.exception.IllegalActionException;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.User;
import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.model.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Override
    public UserResponseDTO findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        ;

        UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getUsername(), user.getRole());
        return dto;
    }

    @Override
    public UserResponseDTO save(UserRegistrationDTO userRegistrationDTO, String username) {
        try {
            findByUsername(userRegistrationDTO.getUsername());
            throw new UserAlreadyExistsException("User already exists!");
        } catch (UserNotFoundException e) {
            if (username != null && !username.isBlank()) {
                throw new IllegalActionException("Cannot create multiple accounts");
            }

            User userEntity = new User();
            userEntity.setUsername(userRegistrationDTO.getUsername());
            userEntity.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
            userEntity.setRole("USER");
            userMapper.save(userEntity);
        }

        return findByUsername(userRegistrationDTO.getUsername());
    }


}
