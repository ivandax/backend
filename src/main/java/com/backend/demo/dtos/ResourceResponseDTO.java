package com.backend.demo.dtos;

import java.util.List;

public class ResourceResponseDTO<T> {
    private List<T> items;
    private int totalPages;

    private int page;

    private int perPage;

    public ResourceResponseDTO() {
    }

    public ResourceResponseDTO(List<T> items, int totalPages, int page, int perPage) {
        this.items = items;
        this.totalPages = totalPages;
        this.page = page;
        this.perPage = perPage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
}
