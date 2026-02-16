package com.java.test.junior.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private Long totalElements;
    private boolean hasNext;

    public PageResponse(List<T> content, int currentPage, int size, Long totalElements, int totalPages) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = currentPage < this.totalPages;
    }
}