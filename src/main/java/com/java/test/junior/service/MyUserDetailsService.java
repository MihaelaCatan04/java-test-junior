package com.java.test.junior.service;

import com.java.test.junior.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.java.test.junior.model.User userFromDb = userMapper.findByUsername(username);

        if (userFromDb == null) {
            throw new UsernameNotFoundException("User not in database!");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(userFromDb.getUsername())
                .password(userFromDb.getPassword())
                .roles("USER")
                .build();
    }
}