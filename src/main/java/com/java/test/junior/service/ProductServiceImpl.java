/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.exception.IllegalArgumentException;
import com.java.test.junior.exception.ProductNotFoundException;
import com.java.test.junior.exception.UserNotLoggedInException;
import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserMapper userMapper;
    private final InteractionMapper interactionMapper;

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
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
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
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
        Product productFromDb = productMapper.findById(id);
        if (productFromDb == null) {
            throw new ProductNotFoundException("Product not found");
        }
        productMapper.deleteProduct(id);
    }

    @Override
    public List<ProductDTO> getPaginatedProducts(int page, int size) {
        RowBounds rowBounds = new RowBounds((page - 1) * size, size);
        if (page < 0 || size < 0) {
            throw new IllegalArgumentException("Page and size must be positive");
        }
        return productMapper.getPaginatedProducts(rowBounds);
    }

    @Override
    public ProductDTO getProductByName(String name) {
        Product product = productMapper.getProductByName(name);
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setDescription(product.getDescription());
        return productDTO;
    }

    @Override
    public void likeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();

        if (productMapper.findById(productId) == null) {
            throw new ProductNotFoundException("Product not found");
        }
        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);

        if (currentInteraction != null && currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        }

        interactionMapper.insertInteraction(userId, productId, true);
    }

    @Override
    public void dislikeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();

        if (productMapper.findById(productId) == null) {
            throw new ProductNotFoundException("Product not found");
        }

        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);

        if (currentInteraction != null && !currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        }

        interactionMapper.insertInteraction(userId, productId, false);
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UserNotLoggedInException("User not logged in");
        }
        return user.getId();
    }
}