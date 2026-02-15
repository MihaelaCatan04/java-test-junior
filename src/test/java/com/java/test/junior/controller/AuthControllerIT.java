package com.java.test.junior.controller;

import com.java.test.junior.BaseIntegrationTest;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    private TestRestTemplate getAuthRestTemplate(String username, String password) {
        UserDTO user = new UserDTO();
        user.setUsername(username);
        user.setPassword(password);
        restTemplate.postForEntity("/auth/register", user, Void.class);
        return restTemplate.withBasicAuth(username, password);
    }

    @Test
    void testRegisterNewUserUnauthenticated() {
        UserDTO user = new UserDTO();
        user.setUsername("unauth_user");
        user.setPassword("pass123");

        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/register", user, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userMapper.findByUsername("unauth_user")).isNotNull();
    }

    @Test
    void testRegisterWhileAuthenticated() {
        UserDTO user = new UserDTO();
        user.setUsername("auth_user");
        user.setPassword("pass123");

        TestRestTemplate authTemplate = getAuthRestTemplate("testuser", "password");

        ResponseEntity<Void> response = authTemplate.postForEntity("/auth/register", user, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testRegisterSameUserAgainFails() {
        UserDTO user = new UserDTO();
        user.setUsername("duplicate_user");
        user.setPassword("pass123");
        restTemplate.postForEntity("/auth/register", user, Void.class);
        ResponseEntity<String> response = restTemplate.postForEntity("/auth/register", user, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
