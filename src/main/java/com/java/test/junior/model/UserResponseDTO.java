package com.java.test.junior.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponseDTO {
    private Long id;
    private String username;
    private String role;
}
