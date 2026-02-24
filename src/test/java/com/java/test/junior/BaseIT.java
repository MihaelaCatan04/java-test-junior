package com.java.test.junior;

import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIT {

    @Autowired
    protected UserService userService; // Now available to all subclasses

    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setupAdmin() {
        String adminUser = "admin";
        String adminPass = "admin123";

        try {
            userService.getUserByUsername(adminUser);
        } catch (Exception e) {
            // Create user if not exists
            UserRegistrationDTO dto = new UserRegistrationDTO(adminUser, adminPass);
            userService.save(dto, null);

            // IMPORTANT: If your userService.save() defaults to 'USER',
            // you must ensure the 'admin' user has the 'ADMIN' role in the DB
            // so that ProductServiceImpl.validateAdmin() passes.
            // If you have a UserRepository, you might need:
            // userRepository.updateRoleToAdmin(adminUser);
        }
    }
}