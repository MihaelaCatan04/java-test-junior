package com.java.test.junior.mapper;

import com.java.test.junior.model.InteractionKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<InteractionKey> fetchKeysToDelete(@Param("batchSize") int batchSize);

    int deleteByKeys(@Param("keys") List<InteractionKey> keys);
}