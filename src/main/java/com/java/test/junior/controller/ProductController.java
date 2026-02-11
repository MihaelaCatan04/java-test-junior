/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.controller;

import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        productService.modifyProductById(id, productDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDTO> getPaginatedProducts(@RequestParam("page") int page, @RequestParam("page_size") int size) {
        return productService.getPaginatedProducts(page, size);
    }

    @GetMapping("name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDTO getProductByName( @PathVariable String name) {
        return productService.getProductByName(name);
    }
}