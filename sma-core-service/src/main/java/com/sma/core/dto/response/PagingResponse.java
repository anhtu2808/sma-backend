package com.sma.core.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PagingResponse<T> {

    public static <T> PagingResponse<T> fromPage(Page<T> page) {
        return PagingResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    // Danh sách dữ liệu của page hiện tại
    List<T> content;

    // Số trang hiện tại (0-based hoặc 1-based tùy bạn)
    int pageNumber;

    // Số phần tử mỗi trang
    int pageSize;

    // Tổng số phần tử
    long totalElements;

    // Tổng số trang
    int totalPages;

    // Có phải trang đầu không
    boolean first;

    // Có phải trang cuối không
    boolean last;
}
