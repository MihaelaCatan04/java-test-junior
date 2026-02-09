/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.controller;

import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        productService.modifyProductById(id, productDTO);
    }
}