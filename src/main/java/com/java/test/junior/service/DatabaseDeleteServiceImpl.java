package com.java.test.junior.service;

import com.java.test.junior.mapper.InteractionMapper;
import com.java.test.junior.model.InteractionKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseDeleteServiceImpl implements DatabaseDeleteService {

    private final InteractionMapper interactionMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int performManagedBatch(int batchSize) {
        List<InteractionKey> keys = interactionMapper.fetchKeysToDelete(batchSize);
        if (keys.isEmpty()) return 0;

        return interactionMapper.deleteByKeys(keys);
    }
}
