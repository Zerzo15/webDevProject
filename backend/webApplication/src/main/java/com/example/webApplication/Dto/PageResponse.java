package com.example.webApplication.Dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * A page of results with the numbers needed for the line "Showing from-to of totalElements results".
 * page is zero-based; from and to are one-based (both 0 when the page is empty).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long from,
        long to) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent().stream().map(mapper).toList();
        long from = content.isEmpty() ? 0 : page.getPageable().getOffset() + 1;
        long to = content.isEmpty() ? 0 : page.getPageable().getOffset() + content.size();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), from, to);
    }
}
