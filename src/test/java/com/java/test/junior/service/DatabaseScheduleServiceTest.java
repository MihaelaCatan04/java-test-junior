package com.java.test.junior.service;

import com.java.test.junior.service.database.DatabaseDeleteService;
import com.java.test.junior.service.database.DatabaseScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseScheduleServiceTest {

    @Mock
    private DatabaseDeleteService deleteService;

    @InjectMocks
    private DatabaseScheduleServiceImpl scheduleService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(scheduleService, "batchSize", 10);
        ReflectionTestUtils.setField(scheduleService, "maxDurationMillis", 2000);
    }

    @Test
    void shouldStopAfterThreeConsecutiveFailures() {
        when(deleteService.performManagedBatch(anyInt()))
                .thenThrow(new RuntimeException("DB Connection Failed"));

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(3)).performManagedBatch(10);
    }

    @Test
    void shouldStopWhenNoMoreRecordsFound() {
        when(deleteService.performManagedBatch(10))
                .thenReturn(10)
                .thenReturn(0);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(2)).performManagedBatch(10);
    }

    @Test
    void shouldResetConsecutiveFailureCounterAfterSuccess() {
        when(deleteService.performManagedBatch(10))
                .thenThrow(new RuntimeException("Transient error"))
                .thenThrow(new RuntimeException("Transient error"))
                .thenReturn(10)
                .thenThrow(new RuntimeException("Transient error"))
                .thenThrow(new RuntimeException("Transient error"))
                .thenReturn(0);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(6)).performManagedBatch(10);
    }

    @Test
    void shouldNotStopBeforeReachingMaxConsecutiveFailures() {
        when(deleteService.performManagedBatch(10))
                .thenThrow(new RuntimeException("Error"))
                .thenThrow(new RuntimeException("Error"))
                .thenReturn(0);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(3)).performManagedBatch(10);
    }

    @Test
    void shouldStopWhenMaxDurationExpires() {
        ReflectionTestUtils.setField(scheduleService, "maxDurationMillis", 1);
        when(deleteService.performManagedBatch(10)).thenReturn(10);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, atMost(3)).performManagedBatch(10);
    }

    @Test
    void shouldHandleEmptyTableOnFirstBatch() {
        when(deleteService.performManagedBatch(10)).thenReturn(0);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(1)).performManagedBatch(10);
    }

    @Test
    void shouldContinueWhenFailuresAreNonConsecutive() {
        when(deleteService.performManagedBatch(10))
                .thenReturn(10)
                .thenThrow(new RuntimeException("Isolated error"))
                .thenReturn(10)
                .thenThrow(new RuntimeException("Isolated error"))
                .thenReturn(10)
                .thenThrow(new RuntimeException("Isolated error"))
                .thenReturn(0);

        scheduleService.hardDeleteOldInteractions();

        verify(deleteService, times(7)).performManagedBatch(10);
    }
}