package com.java.test.junior.model;

import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;



@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
}
