package com.java.test.junior.service.interaction;

import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.model.InteractionKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionMapper interactionMapper;

    public List<InteractionKey> getInteractionsToDelete(int batchSize) {
        return interactionMapper.fetchKeysToDelete(batchSize);
    }

    public int deleteInteractions(List<InteractionKey> keys) {
        return interactionMapper.deleteByKeys(keys);
    }

    public void incrementDeleteAttempts(List<InteractionKey> keys) {
        interactionMapper.incrementDeleteAttempts(keys);
    }

    public int getLikeCount(Long productId) {
        return interactionMapper.getLikeCount(productId);
    }

    public int getDislikeCount(Long productId) {
        return interactionMapper.getDislikeCount(productId);
    }

    public void upsertInteraction(Long userId, Long productId, boolean isLike) {
        interactionMapper.upsertInteraction(userId, productId, isLike);
    }

    public void softDeleteInteraction(Long userId, Long productId) {
        interactionMapper.softDeleteInteraction(userId, productId);
    }

    public Boolean getActiveInteraction(Long userId, Long productId) {
        return interactionMapper.getActiveInteraction(userId, productId);
    }
}
