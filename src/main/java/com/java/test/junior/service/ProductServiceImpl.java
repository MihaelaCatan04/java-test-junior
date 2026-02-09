/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    /**
     * @param productDTO this product to be created
     * @return the product created from the database
     */
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setUserId(1L);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        ProductDTO productToBeReturned = new ProductDTO();
        productToBeReturned.setName(product.getName());
        productToBeReturned.setPrice(product.getPrice());
        productToBeReturned.setDescription(product.getDescription());
        return productToBeReturned;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productMapper.findById(id);
    }

    @Override
    public void modifyProductById(Long id, ProductDTO productDTO) {
        ProductDTO productFromDb = productMapper.findById(id);
        productFromDb.setName(productDTO.getName());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setDescription(productDTO.getDescription());
        productMapper.updateProduct(id, productFromDb);
    }

    @Override
    public void deleteProductById(Long id) {
        productMapper.deleteProduct(id);
    }

    @Override
    public List<ProductDTO> getPaginatedProducts(int page, int size) {
        return productMapper.getPaginatedProducts(page, size);
    }
}