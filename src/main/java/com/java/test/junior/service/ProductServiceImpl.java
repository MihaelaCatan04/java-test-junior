/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.exception.*;
import com.java.test.junior.exception.IllegalArgumentException;
import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.*;
import lombok.RequiredArgsConstructor;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
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

    private static final String COPY_SQL = """
            COPY temp_product (name, price, description) 
            FROM STDIN 
            WITH (FORMAT CSV, HEADER, QUOTE '\"', DELIMITER ',', ENCODING 'UTF8', NULL '')""";
    private static final String CREATE_TEMP_TABLE_SQL = "CREATE TEMP TABLE IF NOT EXISTS temp_product (name TEXT, price NUMERIC, description TEXT)";
    private static final String INSERT_SQL = """
            INSERT INTO product (name, price, description, user_id, created_at, updated_at)
            SELECT name, price, description, ?, NOW(), NOW() 
            FROM temp_product 
            WHERE name IS NOT NULL AND name != ''""";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS temp_product";
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final InteractionMapper interactionMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final UserService userService;


    @Override
    public ProductResponseDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        Long userId = getAuthenticatedUserId();
        product.setUserId(userId);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
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

    @Override
    public ProductResponseDTO modifyProductById(Long id, ProductDTO productDTO, String username) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Product id must be positive");
        Product productFromDb = productMapper.findById(id);
        if (productFromDb == null) throw new ProductNotFoundException("Product not found");
        UserResponseDTO currentUser = userService.findByUsername(username);
        boolean isOwner = Objects.equals(productFromDb.getUserId(), currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        if (!isOwner && !isAdmin) throw new UserAccessDeniedException("You do not have permission to modify this product");
        productFromDb.setName(productDTO.getName());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setDescription(productDTO.getDescription());
        productMapper.updateProduct(id, productFromDb);
        return mapToResponseDTO(productFromDb);
    }

    @Override
    public void deleteProductById(Long id, String username) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Product id must be positive");
        Product productFromDb = productMapper.findById(id);
        if (productFromDb == null) throw new ProductNotFoundException("Product not found");
        UserResponseDTO currentUser = userService.findByUsername(username);
        boolean isOwner = Objects.equals(productFromDb.getUserId(), currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        if (!isOwner && !isAdmin) throw new UserAccessDeniedException("You do not have permission to delete this product");
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
    public int likeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();
        if (productMapper.findById(productId) == null) throw new ProductNotFoundException("Product not found");
        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);
        if (currentInteraction != null && currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        } else {
            interactionMapper.insertInteraction(userId, productId, true);
        }
        return interactionMapper.getLikeCount(productId);
    }

    @Override
    public int dislikeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();
        if (productMapper.findById(productId) == null) throw new ProductNotFoundException("Product not found");
        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);
        if (currentInteraction != null && !currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        } else {
            interactionMapper.insertInteraction(userId, productId, false);
        }
        return interactionMapper.getDislikeCount(productId);
    }

    @Override
    @Transactional
    public void loadProductsFromAddress(String fileAddress) {
        User admin = userMapper.findFirstByRole("ADMIN");
        if (admin == null) {
            throw new RuntimeException("System error: Admin account not found. Please contact support.");
        }

        Long adminId = admin.getId();
        Long authenticatedUserId = getAuthenticatedUserId();

        if (!Objects.equals(adminId, authenticatedUserId)) {
            throw new UserForbiddenException("Only admin can load products");
        }

        try (InputStream inputStream = getInputStreamFromUrl(fileAddress);
             Connection conn = dataSource.getConnection()) {

            BaseConnection pgConn = conn.unwrap(BaseConnection.class);
            CopyManager copyManager = new CopyManager(pgConn);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_TEMP_TABLE_SQL);
                copyManager.copyIn(COPY_SQL, inputStream);

                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
                    pstmt.setLong(1, adminId);
                    pstmt.executeUpdate();
                }
                stmt.execute(DROP_TABLE);
            }
        } catch (Exception e) {
            throw new RuntimeException("Bulk load failed: " + e.getMessage(), e);
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
            throw new FileNotFoundException("Failed to load file from URL: " + fileAddress);
        }
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.findByUsername(username);
        if (user == null) throw new UserNotLoggedInException("User not logged in");
        return user.getId();
    }

    private String getUsernameById(Long id) {
        return userMapper.findUsernameById(id);
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getPrice(),
                product.getDescription(), product.getUserId(), getUsernameById(product.getUserId()));
    }



}