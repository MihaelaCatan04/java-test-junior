/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.exception.ProductNotFoundException;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
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
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setDescription(product.getDescription());
        return productDTO;
    }

    @Override
    public void modifyProductById(Long id, ProductDTO productDTO) {
        Product productFromDb = productMapper.findById(id);
        if (productFromDb == null) {
            throw new ProductNotFoundException("Product not found");
        }
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
        RowBounds rowBounds = new RowBounds((page - 1) * size, size);
        return productMapper.getPaginatedProducts(rowBounds);
    }
}