package com.java.test.junior.service.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class DatabaseCleanupSchedulerImpl implements DatabaseCleanupScheduler {
    private final DatabaseScheduleService databaseScheduleService;

    @Scheduled(cron = "${app.database.cleanup-cron}")
    public void scheduledHardDelete() {
        log.info("Triggered scheduled hard delete job");
        databaseScheduleService.hardDeleteOldInteractions();
    }
}