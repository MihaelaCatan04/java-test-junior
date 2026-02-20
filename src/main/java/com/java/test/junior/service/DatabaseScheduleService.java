package com.java.test.junior.service;

import org.springframework.scheduling.annotation.Scheduled;

public interface DatabaseScheduleService {
    public void hardDeleteOldInteractions();
}
