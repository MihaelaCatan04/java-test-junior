package com.java.test.junior.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {

    private final DatabaseDeleteService databaseDeleteService;
    private static final int BATCH_SIZE = 5000;
    private static final long MAX_DURATION_MILLIS = 4 * 60 * 60 * 1000;
    private static final int THREAD_SLEEP = 500;

    private static final Logger log = LoggerFactory.getLogger(DatabaseScheduleServiceImpl.class);
    private static final int MAX_RETRIES = 3;
    private static final int DELAY_BETWEEN_RETRIES = 1000;

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void hardDeleteOldInteractions() {
        log.info("Starting hardDeleteOldInteractions task");
        long startTime = System.currentTimeMillis();

        int totalDeleted = runBatchLoop();

        log.info("Finished hardDeleteOldInteractions task. Total deleted: {}, duration: {}ms",
                totalDeleted, System.currentTimeMillis() - startTime);
    }

    private int runBatchLoop() {
        int totalDeleted = 0;
        long startTime = System.currentTimeMillis();

        while (!isTimeExpired(startTime)) {
            int deletedCount = runSingleBatch();
            if (deletedCount < 0) break;
            if (deletedCount == 0) {
                log.info("No more records to delete, stopping task");
                break;
            }
            totalDeleted += deletedCount;
            sleepBetweenBatches();
        }

        return totalDeleted;
    }

    private int runSingleBatch() {
        for (int attempts = 1; attempts <= MAX_RETRIES; attempts++) {
            try {
                int deleted = databaseDeleteService.performManagedBatch(BATCH_SIZE);
                log.debug("Deleted {} records in this batch", deleted);
                return deleted;
            } catch (Exception e) {
                log.warn("Batch delete failed, attempt {}/{}", attempts, MAX_RETRIES, e);
                if (attempts == MAX_RETRIES) {
                    log.error("Batch delete failed after {} attempts, stopping early", MAX_RETRIES);
                    break;
                }
                sleepBetweenBatches();
            }
        }
        return -1;
    }

    private boolean isTimeExpired(long startTime) {
        return System.currentTimeMillis() - startTime >= MAX_DURATION_MILLIS;
    }

    private void sleepBetweenBatches() {
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}