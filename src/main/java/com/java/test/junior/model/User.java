package com.java.test.junior.model;

import lombok.*;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {
    private Long id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String role;
}
