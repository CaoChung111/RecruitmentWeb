package com.caochung.recruitment.ai.service.impl;

import com.caochung.recruitment.ai.dto.SemanticSearchResultDTO;
import com.caochung.recruitment.ai.service.SemanticSearchService;
import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SEMANTIC-SEARCH-SERVICE")
public class SemanticSearchServiceImpl implements SemanticSearchService {

    private final JobRepository jobRepository;
    private final VectorStore vectorStore;

    @Override
    public void indexAllJobs() {
        log.info("Starting batch indexing all active Jobs into Vector Store...");
        List<Job> jobs = jobRepository.findAllByActive(JobStatusEnum.OPEN);

        List<Document> documents = jobs.stream()
                .map(job -> {
                    String cleanDesc = job.getDescription() != null
                            ? job.getDescription().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim()
                            : "";
                    String textToEmbed = String.format("""
                            Job Title: %s
                            Level: %s
                            Location: %s
                            Salary: %.2f
                            Job Description: %s
                            """,
                            job.getName(),
                            job.getLevel(),
                            job.getLocation(),
                            job.getSalary(),
                            cleanDesc
                    );
                    Map<String, Object> metaData = new HashMap<>();
                    metaData.put("jobId", job.getId());
                    metaData.put("jobName", job.getName() != null ? job.getName() : "");
                    metaData.put("companyName", job.getCompany() != null ? job.getCompany().getName() : "N/A");
                    metaData.put("companyLogo", (job.getCompany() != null && job.getCompany().getLogo() != null) ? job.getCompany().getLogo() : "");
                    metaData.put("location", job.getLocation() != null ? job.getLocation() : "");
                    metaData.put("salary", job.getSalary());
                    metaData.put("level", job.getLevel() != null ? job.getLevel().name() : "");
                    metaData.put("description", cleanDesc.length() > 220 ? cleanDesc.substring(0, 220) + "..." : cleanDesc);
                    metaData.put("type", "JOB");

                    return new Document(String.valueOf(job.getId()), textToEmbed, metaData);
                }).toList();
        vectorStore.add(documents);
        log.info("Successfully indexed {} jobs into Vector Store", documents.size());
    }

    @Override
    public List<SemanticSearchResultDTO> searchJobsSemantically(String query, int topK, double minSimilarity) {
        log.info("Executing Semantic Search with query='{}', topK={}, minSimilarity={}", query, topK, minSimilarity);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(minSimilarity)
                .build();

        List<Document> matchedDocs = vectorStore.similaritySearch(searchRequest);

        return matchedDocs.stream().map(doc -> new SemanticSearchResultDTO(
                doc.getId(),
                (String) doc.getMetadata().getOrDefault("jobName", "N/A"),
                doc.getText(),
                doc.getScore() != null ? doc.getScore() : 0.0,
                doc.getMetadata()
        )).toList();
    }
}
