package com.java.test.junior.exception;

public class PageExceedsLimit extends RuntimeException {
    public PageExceedsLimit(String message) {
        super(message);
    }
}
