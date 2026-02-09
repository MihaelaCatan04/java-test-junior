/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Product {
    @NotNull
    private Long id;
    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private Double price;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}