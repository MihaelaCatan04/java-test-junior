package com.java.test.junior.service.interaction;

import com.java.test.junior.model.InteractionKey;

import java.util.List;

public interface InteractionService {
    List<InteractionKey> getInteractionsToDelete(int batchSize);

    int deleteInteractions(List<InteractionKey> keys);

    void incrementDeleteAttempts(List<InteractionKey> keys);

    int getLikeCount(Long productId);

    int getDislikeCount(Long productId);

    void upsertInteraction(Long userId, Long productId, boolean isLike);

    void softDeleteInteraction(Long userId, Long productId);

    Boolean getActiveInteraction(Long userId, Long productId);
}
