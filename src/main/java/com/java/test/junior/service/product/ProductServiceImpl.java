/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service.product;

import com.java.test.junior.exception.*;
import com.java.test.junior.exception.IllegalArgumentException;
import com.java.test.junior.mapper.ProductMapper;
import com.java.test.junior.model.*;
import com.java.test.junior.service.interaction.InteractionService;
import com.java.test.junior.service.user.UserService;
import com.java.test.junior.util.AdminIdInjectorReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    private final ProductMapper productMapper;
    private final DataSource dataSource;
    private final UserService userService;
    private final InteractionService interactionService;

    @Value("${app.admin.default.role}")
    private String adminRole;


    @Override
    public ProductResponseDTO createProduct(ProductDTO productDTO) {
        Long userId = getAuthenticatedUserId();
        Product product = mapToProduct(productDTO, userId);
        productMapper.insert(product);

        return mapToResponseDTO(product);
    }

    private Product mapToProduct(ProductDTO productDTO, Long userId) {
        return new Product(
                productDTO.getName(),
                productDTO.getPrice(),
                productDTO.getDescription(),
                userId,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        validateProductId(id);
        Product product = getProductOrThrow(id);

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
        boolean isAdmin = adminRole.equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new UserAccessDeniedException("You do not have permission to modify this product");
        }
    }

    @Override
    public ProductResponseDTO modifyProductById(Long id, ProductDTO productDTO, String username) {
        validateProductId(id);
        Product productFromDb = getProductOrThrow(id);
        authorizeUser(productFromDb, username);

        updateProductFields(productFromDb, productDTO);
        productMapper.updateProduct(id, productFromDb);

        return mapToResponseDTO(productFromDb);
    }

    private void updateProductFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
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
        validatePage(page, size);
        long total = productMapper.countProducts();
        int totalPages = calculateTotalPages(total, size);
        validatePageLimit(page, total, totalPages);
        List<ProductResponseDTO> content = fetchPaginatedProducts(page, size);

        return new PageResponse<>(content, page, size, total, totalPages);
    }

    private void validatePage(int page, int size) {
        if (page <= 0 || size <= 0) {
            throw new IllegalArgumentException("Page and size must be positive");
        }
    }

    private int calculateTotalPages(long total, int size) {
        return (int) Math.ceil((double) total / size);
    }

    private void validatePageLimit(int page, long total, int totalPages) {
        if (total > 0 && page > totalPages)
            throw new PageExceedsLimit("Page " + page + " does not exist");
    }

    private List<ProductResponseDTO> fetchPaginatedProducts(int page, int size) {
        int offset = (page - 1) * size;

        return productMapper.getPaginatedProducts(offset, size)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
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
        validateProductId(productId);
        getProductOrThrow(productId);
        Boolean currentInteraction = interactionService.getActiveInteraction(userId, productId);

        return processInteraction(userId, productId, isLike, currentInteraction);
    }

    private int processInteraction(Long userId, Long productId,
                                   boolean isLike, Boolean existing) {
        softDeleteOrUpsertInteraction(userId, productId, isLike, existing);

        if (isLike) {
            return interactionService.getLikeCount(productId);
        }

        return interactionService.getDislikeCount(productId);
    }

    private void softDeleteOrUpsertInteraction(Long userId, Long productId,
                                               boolean isLike, Boolean existing) {
        if (Objects.equals(existing, isLike))
            interactionService.softDeleteInteraction(userId, productId);
        else
            interactionService.upsertInteraction(userId, productId, isLike);
    }

    private Long validateAdmin() {
        User admin = userService.getUserByRole(adminRole);
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
            return getResourceAsStream(fileAddress);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + fileAddress, e);
        }
    }

    private InputStream getResourceAsStream(String resourceName) throws IOException {
        if (resourceName.startsWith("http")) {
            return URI.create(resourceName).toURL().openStream();
        }

        return new FileInputStream(resourceName);
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