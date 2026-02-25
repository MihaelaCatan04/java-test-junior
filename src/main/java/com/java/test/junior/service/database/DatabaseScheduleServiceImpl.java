package com.java.test.junior.service.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {

    private final DatabaseDeleteService databaseDeleteService;

    private static final int THREAD_SLEEP = 500;
    private static final long INITIAL_BACKOFF = 500;   // 500 ms
    private static final long MAX_BACKOFF = 10000;

    @Value("${app.database.batch-size}")
    private int batchSize;

    @Value("${app.database.max-duration-millis}")
    private long maxDurationMillis;

    @Override
    public void hardDeleteOldInteractions() {
        log.info("Starting hardDeleteOldInteractions task");
        long startTime = System.currentTimeMillis();
        int totalDeleted = runBatchLoop();
        log.info("Finished task. Total deleted: {}, duration: {}ms", totalDeleted, System.currentTimeMillis() - startTime);
    }

    private int runBatchLoop() {
        int totalDeleted = 0;
        long startTime = System.currentTimeMillis();
        int failureCount = 0;

        while (isTimeNotExpired(startTime)) {
            try {
                int deleted = executeDeletionStep();
                if (deleted <= 0) {
                    break;
                }
                totalDeleted += deleted;
                failureCount = 0;
                sleepBetweenBatches();

            } catch (Exception e) {
                failureCount++;
                handleBatchFailure(e, failureCount, startTime);
            }
        }
        return totalDeleted;
    }

    private int executeDeletionStep() {
        int deletedCount = databaseDeleteService.performManagedBatch(batchSize);

        if (deletedCount == 0) {
            log.info("Cleanup complete: No more records found.");
        }

        return deletedCount;
    }

    private void handleBatchFailure(Exception e, int failureCount, long startTime) {
        log.error("Batch failed. Reason: {}", e.getMessage());

        if (isTimeNotExpired(startTime)) {
            sleepExponentially(failureCount);
        }
    }

    private boolean isTimeNotExpired(long startTime) {
        return System.currentTimeMillis() - startTime <= maxDurationMillis;
    }

    private void sleepBetweenBatches() {
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sleepExponentially(int failureCount) {
        long delay = INITIAL_BACKOFF * (1L << failureCount);

        delay = Math.min(delay, MAX_BACKOFF);

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}