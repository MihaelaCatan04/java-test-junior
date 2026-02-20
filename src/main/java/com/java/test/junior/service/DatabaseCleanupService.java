package com.java.test.junior.service;

import com.java.test.junior.mapper.InteractionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseCleanupService {

    private final InteractionMapper interactionMapper;

    @Scheduled(cron = "0 2 * * * *")
    @Transactional
    public int hardDeleteOldInteractions() {
        return interactionMapper.deleteMarkedAsDeleted();
    }
}