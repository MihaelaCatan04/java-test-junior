package com.java.test.junior.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {
    private final DatabaseDeleteService databaseDeleteService;

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void hardDeleteOldInteractions() {
        int batchSize = 5000;
        boolean workRemaining = true;

        while (workRemaining) {
            int deletedCount = databaseDeleteService.performManagedBatch(batchSize);
            if (deletedCount == 0) {
                workRemaining = false;
            }
        }
    }
}
