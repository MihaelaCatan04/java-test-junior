/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.controller;

import com.java.test.junior.model.LoadingDTO;
import com.java.test.junior.model.PageResponse;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.ProductResponseDTO;
import com.java.test.junior.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
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
@Tag(name = "Products", description = "Product-related endpoints")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Get a product by id")
    public ProductResponseDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a product by id")
    public ProductResponseDTO updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO, Principal principal) {
        return productService.modifyProductById(id, productDTO, principal.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a product by id")
    public void deleteProduct(@PathVariable Long id, Principal principal) {
        productService.deleteProductById(id, principal.getName());
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get products paginated")
    public PageResponse<ProductResponseDTO> getPaginatedProducts(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "page_size", defaultValue = "3") int size) {
        return productService.getPaginatedProducts(page, size);
    }

    @GetMapping("name/{name}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get product by name")
    public List<ProductResponseDTO> getProductByName(@PathVariable String name) {
        return productService.getProductByName(name);
    }

    @PostMapping("/{id}/like")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Like a product")
    public int likeProduct(@PathVariable Long id) {
        return productService.likeProduct(id);
    }

    @PostMapping("{id}/dislike")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Dislike a product")
    public int dislikeProduct(@PathVariable Long id) {

        return productService.dislikeProduct(id);
    }

    @PostMapping("/loading/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Load products from a local or remote CSV path")
    public void loadProducts(@RequestBody LoadingDTO loadingDTO) throws IOException {
        productService.loadProductsFromAddress(loadingDTO.getFileAddress());
    }
}