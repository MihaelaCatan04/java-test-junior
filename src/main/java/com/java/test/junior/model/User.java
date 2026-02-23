package com.java.test.junior.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String role;
}
