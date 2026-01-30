package com.caochung.recruitment.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class Meta {
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPage;
}
