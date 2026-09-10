package com.caochung.recruitment.ai.service;

import com.caochung.recruitment.ai.dto.SemanticSearchResultDTO;

import java.util.List;

public interface SemanticSearchService {
    void indexAllJobs();

    List<SemanticSearchResultDTO> searchJobsSemantically(String query, int topK, double minSimilarity);
}
