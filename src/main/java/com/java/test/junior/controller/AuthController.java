package com.java.test.junior.controller;

import com.java.test.junior.model.UserRegistrationDTO;
import com.java.test.junior.model.UserResponseDTO;
import com.java.test.junior.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Authentication-related endpoints")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO, Principal principal) {
        if (principal == null) {
            return userService.save(userRegistrationDTO, null);
        }

        return userService.save(userRegistrationDTO, principal.getName());
    }
}
