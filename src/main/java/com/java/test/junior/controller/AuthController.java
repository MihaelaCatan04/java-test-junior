package com.java.test.junior.controller;

import com.java.test.junior.model.UserDTO;
import com.java.test.junior.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public void register(@Valid @RequestBody UserDTO userDTO) {
        userService.save(userDTO);
    }
}
