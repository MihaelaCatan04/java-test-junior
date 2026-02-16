/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.controller.FileNotFoundException;
import com.java.test.junior.controller.IllegalArgumentException;
import com.java.test.junior.controller.ProductNotFoundException;
import com.java.test.junior.controller.UserNotLoggedInException;
import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.mapper.UserMapper;
import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${app.admin.default-username}")
    private String defaultAdminUsername;
    @Value("${app.admin.default-password}")
    private String defaultAdminPassword;

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
        return mapToDTO(product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Product id must be positive");
        Product product = productMapper.findById(id);
        if (product == null) throw new ProductNotFoundException("Product not found");
        return mapToDTO(product);
    }

    @Override
    public void modifyProductById(Long id, ProductDTO productDTO) {
        Product productFromDb = productMapper.findById(id);
        if (productFromDb == null) throw new ProductNotFoundException("Product not found");
        productFromDb.setName(productDTO.getName());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setDescription(productDTO.getDescription());
        productMapper.updateProduct(id, productFromDb);
    }

    @Override
    public void deleteProductById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Product id must be positive");
        if (productMapper.findById(id) == null) throw new ProductNotFoundException("Product not found");
        productMapper.deleteProduct(id);
    }

    @Override
    public List<ProductDTO> getPaginatedProducts(int page, int size) {
        if (page < 1 || size < 1) throw new IllegalArgumentException("Page and size must be positive");
        RowBounds rowBounds = new RowBounds((page - 1) * size, size);
        return productMapper.getPaginatedProducts(rowBounds);
    }

    @Override
    public ProductDTO getProductByName(String name) {
        Product product = productMapper.getProductByName(name);
        if (product == null) throw new ProductNotFoundException("Product not found: " + name);
        return mapToDTO(product);
    }

    @Override
    public void likeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();
        if (productMapper.findById(productId) == null) throw new ProductNotFoundException("Product not found");
        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);
        if (currentInteraction != null && currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        } else {
            interactionMapper.insertInteraction(userId, productId, true);
        }
    }

    @Override
    public void dislikeProduct(Long productId) {
        Long userId = getAuthenticatedUserId();
        if (productMapper.findById(productId) == null) throw new ProductNotFoundException("Product not found");
        Boolean currentInteraction = interactionMapper.getExistingInteraction(userId, productId);
        if (currentInteraction != null && !currentInteraction) {
            interactionMapper.removeInteraction(userId, productId);
        } else {
            interactionMapper.insertInteraction(userId, productId, false);
        }
    }

    @Override
    @Transactional
    public void loadProductsFromAddress(String fileAddress) {
        Long adminId = getAdminId();

        try (InputStream inputStream = getInputStreamFromUrl(fileAddress); Connection conn = dataSource.getConnection()) {

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

    private Long getAdminId() {
        User admin = userMapper.findFirstByRole("ADMIN");
        if (admin == null) {
            admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole("ADMIN");
            userMapper.save(admin);
            admin = userMapper.findFirstByRole("ADMIN");
        }
        return admin.getId();
    }

    private Long getAuthenticatedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.findByUsername(username);
        if (user == null) throw new UserNotLoggedInException("User not logged in");
        return user.getId();
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        return dto;
    }
}