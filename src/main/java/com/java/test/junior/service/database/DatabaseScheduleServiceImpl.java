package com.java.test.junior.service.database;

import com.java.test.junior.model.BatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseScheduleServiceImpl implements DatabaseScheduleService {
    private static final long MAX_BACKOFF = 10000;
    private static final long INITIAL_BACKOFF = 500;

    private final DatabaseDeleteService databaseDeleteService;

    @Value("${app.database.batch-size}")
    private int batchSize;
    @Value("${app.database.max-duration-millis}")
    private long maxDurationMillis;

    @Override
    public void hardDeleteOldInteractions() {
        log.info("Starting hardDeleteOldInteractions task");
        long startTime = System.currentTimeMillis();
        int totalDeleted = runBatchLoop(startTime);
        log.info("Finished task. Total deleted: {}, duration: {}ms", totalDeleted, System.currentTimeMillis() - startTime);
    }

    private int runBatchLoop(long startTime) {
        int totalDeleted = 0;
        int failureCount = 0;

        while (isTimeNotExpired(startTime)) {

            BatchResult result = processSingleIteration(failureCount, startTime);
            totalDeleted += result.getDeleted();
            failureCount = result.getFailureCount();
            if (result.isShouldStop()) {
                return totalDeleted;
            }
        }

        return totalDeleted;
    }

    private BatchResult processSingleIteration(int failureCount, long startTime) {
        try {
            return handleSuccess(failureCount);
        } catch (Exception e) {
            return handleFailure(e, ++failureCount, startTime);
        }
    }

    private BatchResult handleSuccess(int failureCount) {
        int deletedRows = executeDeletionStep();
        if (deletedRows == 0) {
            return new BatchResult(0, failureCount, true);
        }
        sleep(0);

        return new BatchResult(deletedRows, 0, false);
    }

    private BatchResult handleFailure(Exception e, int failureCount, long startTime) {
        log.error("Batch failed. Reason: {}", e.getMessage());
        if (isTimeNotExpired(startTime)) {
            sleep(failureCount);
        }

        return new BatchResult(0, failureCount, false);
    }


    private int executeDeletionStep() {
        int deletedCount = databaseDeleteService.performManagedBatch(batchSize);
        if (deletedCount == 0) {
            log.info("Cleanup complete: No more records found.");
        }

        return deletedCount;
    }

    private boolean isTimeNotExpired(long startTime) {
        return System.currentTimeMillis() - startTime <= maxDurationMillis;
    }

    private void sleep(int failureCount) {
        long delay = INITIAL_BACKOFF * (1L << failureCount);
        delay = Math.min(delay, MAX_BACKOFF);

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}