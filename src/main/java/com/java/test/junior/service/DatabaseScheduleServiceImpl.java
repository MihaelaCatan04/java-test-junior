package com.java.test.junior.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {
    private final DatabaseDeleteService databaseDeleteService;
    private final int BATCH_SIZE = 5000;
    private final long MAX_DURATION_MILLIS = 4 * 60 * 60 * 1000;
    private final static int THREAD_SLEEP = 500;

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void hardDeleteOldInteractions() {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            int deletedCount = databaseDeleteService.performManagedBatch(BATCH_SIZE);
            if (deletedCount == 0) {
                break;
            }
            try {
                Thread.sleep(THREAD_SLEEP);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
