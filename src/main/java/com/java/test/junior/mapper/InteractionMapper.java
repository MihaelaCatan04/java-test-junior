package com.java.test.junior.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InteractionMapper {
    Boolean getActiveInteraction(@Param("userId") Long userId,
                                 @Param("productId") Long productId);

    void upsertInteraction(@Param("userId") Long userId,
                           @Param("productId") Long productId,
                           @Param("isLike") boolean isLike);

    void softDeleteInteraction(@Param("userId") Long userId,
                               @Param("productId") Long productId);

    int getLikeCount(@Param("productId") Long productId);

    int getDislikeCount(@Param("productId") Long productId);

    int deleteMarkedAsDeleted();
}