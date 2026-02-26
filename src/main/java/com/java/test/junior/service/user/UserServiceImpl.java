package com.java.test.junior.service.user;

import com.java.test.junior.exception.IllegalActionException;
import com.java.test.junior.exception.UserAlreadyExistsException;
import com.java.test.junior.exception.UserNotFoundException;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.User;
import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.model.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    @Value("${app.user.default.role}")
    private String defaultRole;

    @Override
    public UserResponseDTO findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return new UserResponseDTO(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public UserResponseDTO save(UserRegistrationDTO userRegistrationDTO, String username) {
        try {
            findByUsername(userRegistrationDTO.getUsername());
            throw new UserAlreadyExistsException("User already exists!");
        } catch (UserNotFoundException e) {
            checkMultipleAccounts(username);
            handleUserCreation(userRegistrationDTO);
        }

        return findByUsername(userRegistrationDTO.getUsername());
    }

    private void checkMultipleAccounts(String username) {
        if (username != null && !username.isBlank()) {
            throw new IllegalActionException("Cannot create multiple accounts");
        }
    }

    private void handleUserCreation(UserRegistrationDTO userRegistrationDTO) {
        User userEntity = new User(userRegistrationDTO.getUsername(), passwordEncoder.encode(userRegistrationDTO.getPassword()), defaultRole);
        userMapper.save(userEntity);
    }

    public String getUsernameById(Long id) {
        return userMapper.findUsernameById(id);
    }

    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User getUserByRole(String role) {
        return userMapper.findFirstByRole(role);
    }
}
