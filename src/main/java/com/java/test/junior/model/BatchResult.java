package com.java.test.junior.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class BatchResult {
    private int deleted;
    private int failureCount;
    private boolean shouldStop;
}
