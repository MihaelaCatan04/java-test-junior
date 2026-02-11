package com.java.test.junior.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InteractionMapper {
    void insertInteraction(@Param("userId") Long userId,
                           @Param("productId") Long productId,
                           @Param("isLike") boolean isLike);

    Boolean getExistingInteraction(@Param("userId") Long userId,
                                   @Param("productId") Long productId);

    void removeInteraction(@Param("userId") Long userId,
                           @Param("productId") Long productId);
}
