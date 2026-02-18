/*
 * Copyright (c) 2013-2022 Global Database Ltd, All rights reserved.
 */

package com.java.test.junior.mapper;

import com.java.test.junior.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    void updateProduct(@Param("id") Long id, @Param("product") Product product);

    void deleteProduct(Long id);

    List<Product> getPaginatedProducts(int offset, int size);

    List<Product> getProductByName(String name);

    Long countProducts();

    void copy(List<Product> batch);

}