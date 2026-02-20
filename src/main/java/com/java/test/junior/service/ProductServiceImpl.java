/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.exception.*;
import com.java.test.junior.exception.IllegalArgumentException;
import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.model.*;
import com.java.test.junior.util.AdminIdInjectorReader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /**
     * SQL Constants
     * The FORMAT CSV syntax is robust against multi-line descriptions and special characters.
     */

    private final ProductMapper productMapper;
    private final InteractionMapper interactionMapper;
    private final DataSource dataSource;
    private final UserService userService;


    @Override
    public ProductResponseDTO createProduct(ProductDTO productDTO) {
        Long userId = getAuthenticatedUserId();
        Product product = new Product(productDTO.getName(), productDTO.getPrice(), productDTO.getDescription(), userId, LocalDateTime.now(), LocalDateTime.now());
        productMapper.insert(product);
        return mapToResponseDTO(product);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Product id must be positive");
        Product product = productMapper.findById(id);
        if (product == null) throw new ProductNotFoundException("Product not found");
        return mapToResponseDTO(product);
    }

    private void validateProductId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
    }

    private Product getProductOrThrow(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }
        return product;
    }

    private void authorizeUser(Product product, String username) {
        UserResponseDTO currentUser = userService.findByUsername(username);
        boolean isOwner = Objects.equals(product.getUserId(), currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new UserAccessDeniedException("You do not have permission to modify this product");
        }
    }

    @Override
    public ProductResponseDTO modifyProductById(Long id, ProductDTO productDTO, String username) {
        validateProductId(id);
        Product productFromDb = getProductOrThrow(id);
        authorizeUser(productFromDb, username);

        productFromDb.setName(productDTO.getName());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setDescription(productDTO.getDescription());

        productMapper.updateProduct(id, productFromDb);
        return mapToResponseDTO(productFromDb);
    }

    @Override
    public void deleteProductById(Long id, String username) {
        validateProductId(id);
        Product productFromDb = getProductOrThrow(id);
        authorizeUser(productFromDb, username);

        productMapper.deleteProduct(id);
    }

    @Override
    public PageResponse<ProductResponseDTO> getPaginatedProducts(int page, int size) {
        if (page < 1 || size < 1) throw new IllegalArgumentException("Page and size must be positive");
        Long totalCount = productMapper.countProducts();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < page) {
            throw new PageExceedsLimit("Page " + page + " does not exist");
        }
        int offset = (page - 1) * size;
        List<Product> products = productMapper.getPaginatedProducts(offset, size);
        List<ProductResponseDTO> productDTOS = products.stream().map(this::mapToResponseDTO).toList();
        return new PageResponse<>(productDTOS, page, size, totalCount, totalPages);
    }

    @Override
    public List<ProductResponseDTO> getProductByName(String name) {
        List<Product> product = productMapper.getProductByName(name);
        return product.stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    @Transactional
    public int handleInteraction(Long productId, boolean isLike) {
        Long userId = getAuthenticatedUserId();

        if (productMapper.findById(productId) == null) {
            throw new ProductNotFoundException("Product not found");
        }

        Boolean currentInteraction = interactionMapper.getActiveInteraction(userId, productId);

        if (currentInteraction != null && currentInteraction == isLike) {
            interactionMapper.softDeleteInteraction(userId, productId);
        } else {
            interactionMapper.upsertInteraction(userId, productId, isLike);
        }

        return isLike ? interactionMapper.getLikeCount(productId)
                : interactionMapper.getDislikeCount(productId);
    }

    private Long validateAdmin() {
        User admin = userService.getUserByRole("ADMIN");
        Long adminId = admin.getId();
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!Objects.equals(adminId, authenticatedUserId)) {
            throw new UserForbiddenException("Only admin can load products");
        }
        return adminId;
    }

    @Override
    @Transactional
    public void loadProductsFromAddress(String fileAddress) {
        Long adminId = validateAdmin();
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try (InputStream inputStream = getInputStreamFromUrl(fileAddress); Reader sourceReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8); AdminIdInjectorReader injectedReader = new AdminIdInjectorReader(sourceReader, adminId)) {
            productMapper.copy(injectedReader);

        } catch (Exception e) {
            throw new RuntimeException("Bulk product load failed: " + e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private InputStream getInputStreamFromUrl(String fileAddress) {
        try {
            if (fileAddress.startsWith("http")) {
                return URI.create(fileAddress).toURL().openStream();
            } else {
                return new FileInputStream(fileAddress);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + fileAddress, e);
        }
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        if (user == null) throw new UserNotLoggedInException("User not logged in");
        return user.getId();
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getPrice(), product.getDescription(), product.getUserId(), userService.getUsernameById(product.getUserId()));
    }


}