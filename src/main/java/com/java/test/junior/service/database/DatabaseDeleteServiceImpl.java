package com.java.test.junior.service.database;

import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.model.InteractionKey;
import com.java.test.junior.service.interaction.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseDeleteServiceImpl implements DatabaseDeleteService {

    private final InteractionService interactionService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int performManagedBatch(int batchSize) {
        List<InteractionKey> keys = interactionService.getInteractionsToDelete(batchSize);
        if (keys.isEmpty()) return 0;
        try {
            return interactionService.deleteInteractions(keys);
        } catch (Exception e) {
            interactionService.incrementDeleteAttempts(keys);
            throw e;
        }
    }
}