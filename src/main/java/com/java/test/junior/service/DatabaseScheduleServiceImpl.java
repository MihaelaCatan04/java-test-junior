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
        long maxDurationMillis = 4 * 60 * 60 * 1000;
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < maxDurationMillis) {
            int deletedCount = databaseDeleteService.performManagedBatch(batchSize);
            if (deletedCount == 0) {
                break;
            }
        }
    }
}
