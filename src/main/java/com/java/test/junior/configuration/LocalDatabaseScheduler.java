package com.java.test.junior.configuration;

import com.java.test.junior.service.database.DatabaseScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDatabaseScheduler {
    private final DatabaseScheduleService databaseScheduleService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runCleanup() {
        databaseScheduleService.hardDeleteOldInteractions();
    }
}
