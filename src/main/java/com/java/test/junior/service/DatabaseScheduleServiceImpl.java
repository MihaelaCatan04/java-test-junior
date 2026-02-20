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

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void hardDeleteOldInteractions() {
        log.info("Starting hardDeleteOldInteractions task");

        long startTime = System.currentTimeMillis();
        int totalDeleted = 0;

        while (System.currentTimeMillis() - startTime < MAX_DURATION_MILLIS) {
            try {
                int deletedCount = databaseDeleteService.performManagedBatch(BATCH_SIZE);
                totalDeleted += deletedCount;
                log.debug("Deleted {} records in this batch", deletedCount);

                if (deletedCount == 0) {
                    log.info("No more records to delete, stopping task");
                    break;
                }

                Thread.sleep(THREAD_SLEEP);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Batch delete failed, stopping early", e);
                break;
            }
        }

        log.info("Finished hardDeleteOldInteractions task. Total deleted: {}, duration: {}ms",
                totalDeleted, System.currentTimeMillis() - startTime);
    }
}