package com.example.webApplication.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.webApplication.Exception.InvalidRequestException;

/**
 * Builds a Pageable from the list parameters: page size can only be 10, 20, 50 or 100 and
 * sorting by several columns is written as sort=field,dir&sort=field,dir (proposal 5.2).
 * Only the fields the caller allows can be sorted, so a client cannot sort by arbitrary properties.
 */
final class PagingSupport {

    static final Set<Integer> PAGE_SIZES = Set.of(10, 20, 50, 100);

    private PagingSupport() {
    }

    /** The values of every sort=... parameter, exactly as sent (not split at commas). */
    static List<String> rawSortParams(HttpServletRequest request) {
        String[] values = request.getParameterValues("sort");
        return values == null ? List.of() : List.of(values);
    }

    static Pageable pageable(int page, int size, List<String> sorts, Set<String> allowedFields, Sort defaultSort) {
        if (page < 0) {
            throw new InvalidRequestException("page must be zero or more");
        }
        if (!PAGE_SIZES.contains(size)) {
            throw new InvalidRequestException("size must be one of 10, 20, 50, 100");
        }
        if (sorts == null || sorts.isEmpty()) {
            return PageRequest.of(page, size, defaultSort);
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            if (!allowedFields.contains(field)) {
                throw new InvalidRequestException("Cannot sort by " + field);
            }
            boolean desc = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc");
            if (parts.length > 1 && !desc && !parts[1].trim().equalsIgnoreCase("asc")) {
                throw new InvalidRequestException("Sort direction must be asc or desc");
            }
            orders.add(desc ? Sort.Order.desc(field) : Sort.Order.asc(field));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }
}
