package com.java.test.junior.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {

    private final DatabaseDeleteService databaseDeleteService;

    private static final int THREAD_SLEEP = 500;
    private static final int MAX_RETRIES = 3;

    @Value("${app.database.batch-size}")
    private int batchSize;

    @Value("${app.database.max-duration-millis}")
    private long maxDurationMillis;


    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void hardDeleteOldInteractions() {
        log.info("Starting hardDeleteOldInteractions task");
        long startTime = System.currentTimeMillis();
        int totalDeleted = runBatchLoop();
        log.info("Finished hardDeleteOldInteractions task. Total deleted: {}, duration: {}ms", totalDeleted, System.currentTimeMillis() - startTime);
    }

    private int runBatchLoop() {
        int totalDeleted = 0;
        long startTime = System.currentTimeMillis();

        while (!isTimeExpired(startTime)) {
            int deletedCount = runSingleBatch();
            if (deletedCount <= 0) {
                logBatchLoop(deletedCount);
                break;
            }
            totalDeleted += deletedCount;
            sleepBetweenBatches();
        }

        return totalDeleted;
    }
    private void logBatchLoop(int deletedCount) {
        if (deletedCount == -1) {
            log.error("Batch delete failed, stopping task");
        } else {
            log.info("No more records to delete, stopping task");
        }
    }

    private int runSingleBatch() {
        for (int attempts = 1; attempts <= MAX_RETRIES; attempts++) {
            try {
                int deleted = databaseDeleteService.performManagedBatch(batchSize);
                log.debug("Deleted {} records in this batch", deleted);
                return deleted;
            } catch (Exception e) {
                if (!handleBatchFailure(e, attempts)) {
                    return -1;
                }
            }
        }
        log.error("Batch delete failed after max retries");
        return -1;
    }

    private boolean handleBatchFailure(Exception e, int attempts) {

        log.warn("Batch delete failed (attempt {}/{})", attempts, MAX_RETRIES, e);

        if (!isRetryableException(e)) {
            log.error("Non-retryable processing error detected, stopping batch job");
            return false;
        }
        if (attempts < MAX_RETRIES) {
            sleepBackoff(attempts);
            return true;
        }
        return false;
    }

    private void sleepBackoff(int retry) {
        try {
            long delay = Math.min(1000L * (1L << (retry - 1)), 10000L);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isTimeExpired(long startTime) {
        return System.currentTimeMillis() - startTime >= maxDurationMillis;
    }

    private void sleepBetweenBatches() {
        try {
            Thread.sleep(THREAD_SLEEP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isRetryableException(Exception e) {
        return e instanceof java.sql.SQLTransientException || e instanceof java.net.SocketTimeoutException;
    }
}