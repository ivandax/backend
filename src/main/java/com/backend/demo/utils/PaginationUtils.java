package com.backend.demo.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {
    private static final int defaultPage = 0;
    private static final int defaultPerPage = 10;

    public static Pageable getPaginationConfig(Integer pageOrNull, Integer perPageOrNull,
                                               String sortBy, Sort.Direction sortDirection){
        int adjustedPage = pageOrNull == null ? defaultPage : pageOrNull - 1;
        int perPage = perPageOrNull == null ? defaultPerPage : perPageOrNull;
        if(sortBy == null || sortDirection == null){
            return PageRequest.of(adjustedPage, perPage);
        }
        if(sortDirection.isAscending()){
            return PageRequest.of(adjustedPage, perPage, Sort.by(sortBy).ascending());
        } else {
            return PageRequest.of(adjustedPage, perPage, Sort.by(sortBy).descending());
        }
    }

    public static int getPage(Integer pageOrNull){
        return pageOrNull == null ? defaultPage + 1 : pageOrNull;
    }

    public static int getPerPage(Integer perPageOrNull){
        return perPageOrNull == null ? defaultPerPage : perPageOrNull;
    }
}
