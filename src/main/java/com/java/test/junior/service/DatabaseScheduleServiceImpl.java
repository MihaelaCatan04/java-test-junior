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

    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private static final int THREAD_SLEEP = 500;

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
        log.info("Finished task. Total deleted: {}, duration: {}ms",
                totalDeleted, System.currentTimeMillis() - startTime);
    }

    private int runBatchLoop() {
        int totalDeleted = 0;
        int consecutiveFailures = 0;
        long startTime = System.currentTimeMillis();

        while (!isTimeExpired(startTime) && consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
            try {
                int deletedCount = databaseDeleteService.performManagedBatch(batchSize);

                if (deletedCount == 0) {
                    log.info("Cleanup complete: No more records found.");
                    break;
                }

                totalDeleted += deletedCount;
                consecutiveFailures = 0;
                sleepBetweenBatches();

            } catch (Exception e) {
                consecutiveFailures++;
                log.error("Batch failed ({}/{}). Reason: {}",
                        consecutiveFailures, MAX_CONSECUTIVE_FAILURES, e.getMessage());
            }
        }
        return totalDeleted;
    }

    private boolean isTimeExpired(long startTime) {
        return System.currentTimeMillis() - startTime >= maxDurationMillis;
    }

    private void sleepBetweenBatches() {
        try { Thread.sleep(THREAD_SLEEP); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}