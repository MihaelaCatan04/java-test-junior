package com.java.test.junior.configuration;

import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Value("${app.admin.default-username}")
    private String defaultAdminUsername;
    @Value("${app.admin.default-password}")
    private String defaultAdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        if (userMapper.findFirstByRole("ADMIN") == null) {
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole("ADMIN");
            userMapper.save(admin);
            System.out.println("System Admin initialized successfully.");
        }
    }
}
