/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.service;

import com.java.test.junior.model.PageResponse;
import com.java.test.junior.model.ProductDTO;
import com.java.test.junior.model.ProductResponseDTO;

import java.util.List;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */
public interface ProductService {
    /**
     * @param productDTO this product to be created
     * @return the product created from the database
     */
    ProductResponseDTO createProduct(ProductDTO productDTO);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO modifyProductById(Long id, ProductDTO productDTO, String username);

    void deleteProductById(Long id, String username);

    PageResponse<ProductResponseDTO> getPaginatedProducts(int page, int size);

    List<ProductResponseDTO> getProductByName(String name);

    void loadProductsFromAddress(String fileAddress);

    int handleInteraction(Long productId, boolean isLike);
}
