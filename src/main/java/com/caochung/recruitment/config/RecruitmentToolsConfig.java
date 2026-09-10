package com.caochung.recruitment.config;

import com.caochung.recruitment.ai.dto.tool.JobDetailToolRequest;
import com.caochung.recruitment.ai.dto.tool.JobDetailToolResponse;
import com.caochung.recruitment.ai.dto.tool.JobSearchToolRequest;
import com.caochung.recruitment.ai.dto.tool.JobSummaryToolResponse;
import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j(topic = "RECRUITMENT-TOOL")
public class RecruitmentToolsConfig {
    private final JobRepository jobRepository;

    @Bean
    @Description("Tra cứu danh sách công việc hiện có trong hệ thống theo từ khóa kỹ năng, địa điểm, mức lương và cấp bậc.")
    public Function<JobSearchToolRequest, JobSummaryToolResponse> searchJobsFunction() {
        return request -> {
            try {
                log.info("Executing Tool [searchJobsFunction] with args: {}", request);
                String keyword = (request != null && request.keyword() != null && !request.keyword().isBlank())
                        ? request.keyword().trim().toLowerCase() : null;
                String location = (request != null && request.location() != null && !request.location().isBlank())
                        ? request.location().trim().toLowerCase() : null;
                String level = (request != null && request.level() != null && !request.level().isBlank())
                        ? request.level().trim() : null;
                Double minSalary = (request != null) ? request.minSalary() : null;

                List<Job> matchedJobs = jobRepository.findAllByActive(JobStatusEnum.OPEN).stream()
                        .filter(job -> keyword == null ||
                                (job.getName() != null && job.getName().toLowerCase().contains(keyword)) ||
                                (job.getDescription() != null && job.getDescription().toLowerCase().contains(keyword)))
                        .filter(job -> location == null ||
                                (job.getLocation() != null && job.getLocation().toLowerCase().contains(location)))
                        .filter(job -> level == null ||
                                (job.getLevel() != null && job.getLevel().name().equalsIgnoreCase(level)))
                        .filter(job -> minSalary == null || job.getSalary() >= minSalary)
                        .limit(5)
                        .toList();

                List<JobSummaryToolResponse.JobItem> items = matchedJobs.stream()
                        .map(j -> JobSummaryToolResponse.JobItem.builder()
                                .id(j.getId())
                                .title(j.getName() != null ? j.getName() : "N/A")
                                .companyName(j.getCompany() != null && j.getCompany().getName() != null ? j.getCompany().getName() : "N/A")
                                .location(j.getLocation() != null ? j.getLocation() : "N/A")
                                .salary(j.getSalary())
                                .level(j.getLevel() != null ? j.getLevel().name() : "N/A")
                                .build())
                        .toList();

                log.info("Tool [searchJobsFunction] found {} results", items.size());
                return new JobSummaryToolResponse(items.size(), items);
            } catch (Exception e) {
                log.error("Error executing Tool [searchJobsFunction]", e);
                return new JobSummaryToolResponse(0, List.of());
            }
        };
    }

    @Bean(name = {"jobDetailFunction"})
    @Description("Lấy thông tin chi tiết đầy đủ của một công việc cụ thể dựa vào Job ID.")
    public Function<JobDetailToolRequest, JobDetailToolResponse> jobDetailFunction() {
        return request -> {
            try {
                if (request == null || request.jobId() == null) {
                    return JobDetailToolResponse.builder()
                            .status("ERROR")
                            .description("Job ID không hợp lệ hoặc bị thiếu")
                            .build();
                }
                log.info("Executing Tool [getJobDetailFunction] for jobId: {}", request.jobId());
                return jobRepository.findById(request.jobId())
                        .map(job -> JobDetailToolResponse.builder()
                                .id(job.getId())
                                .name(job.getName() != null ? job.getName() : "N/A")
                                .companyName(job.getCompany() != null && job.getCompany().getName() != null ? job.getCompany().getName() : "N/A")
                                .location(job.getLocation() != null ? job.getLocation() : "N/A")
                                .salary(job.getSalary())
                                .level(job.getLevel() != null ? job.getLevel().name() : "N/A")
                                .quantity(job.getQuantity())
                                .description(job.getDescription() != null ? job.getDescription() : "N/A")
                                .status("FOUND")
                                .build())
                        .orElseGet(() -> JobDetailToolResponse.builder()
                                .id(request.jobId())
                                .status("NOT_FOUND")
                                .description("Không tìm thấy công việc với ID: " + request.jobId())
                                .build());
            } catch (Exception e) {
                log.error("Error executing Tool [getJobDetailFunction]", e);
                return JobDetailToolResponse.builder()
                        .id(request != null ? request.jobId() : null)
                        .status("ERROR")
                        .description("Lỗi hệ thống khi tra cứu công việc: " + e.getMessage())
                        .build();
            }
        };
    }
}
