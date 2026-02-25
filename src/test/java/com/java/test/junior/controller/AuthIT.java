package com.java.test.junior.controller;

import com.java.test.junior.BaseIT;
import com.java.test.junior.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthIT extends BaseIT {

    protected static final String ALICE = "alice";
    protected static final String ALICE_PASS = "alice123";
    protected static final String JOHNY = "johny";
    protected static final String JOHNY_PASS = "johny123";

    private static final String AUTH_REGISTER = "/auth/register";

    @Value("${app.admin.default.username}")
    private String admin;
    @Value("${app.admin.default.password}")
    private String adminPass;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void aliceRegistration_returnsCreatedAndBody() {
        ResponseEntity<UserResponseDTO> response =
                restTemplate.postForEntity(
                        AUTH_REGISTER,
                        new UserRegistrationDTO(ALICE, ALICE_PASS),
                        UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponseDTO body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals(ALICE, body.getUsername());
        assertEquals("USER", body.getRole());
    }

    @Test
    @Order(2)
    void johnyRegistration_returnsCreated() {
        ResponseEntity<UserResponseDTO> response =
                restTemplate.postForEntity(
                        AUTH_REGISTER,
                        new UserRegistrationDTO(JOHNY, JOHNY_PASS),
                        UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(response.getBody());
    }

    @Test
    @Order(3)
    void duplicateRegistration_returnsConflict() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(
                        AUTH_REGISTER,
                        new UserRegistrationDTO(ALICE, "anotherPassword"),
                        ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(4)
    void alreadyLoggedInUser_cannotRegisterAnotherAccount() {
        ResponseEntity<ErrorResponse> response =
                restTemplate.withBasicAuth(ALICE, ALICE_PASS)
                        .postForEntity(
                                AUTH_REGISTER,
                                new UserRegistrationDTO("alice2", "pass123"),
                                ErrorResponse.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.CONFLICT, HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
    }

}