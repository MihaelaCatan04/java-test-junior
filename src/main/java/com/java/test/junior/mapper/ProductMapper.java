/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.mapper;

import com.java.test.junior.model.Product;
import com.java.test.junior.model.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author dumitru.beselea
 * @version java-test-junior
 * @apiNote 08.12.2022
 */

@Mapper
public interface ProductMapper {
    Product findById(Long id);
    void insert(Product product);
    void updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    List<ProductDTO> getPaginatedProducts(int page, int size);
}