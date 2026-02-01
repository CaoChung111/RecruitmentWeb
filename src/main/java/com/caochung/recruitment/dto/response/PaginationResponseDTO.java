package com.caochung.recruitment.dto.response;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponseDTO {
    private Meta meta;
    private Object result;

    @Getter @Setter
    @Builder
    public static class Meta {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }
}
