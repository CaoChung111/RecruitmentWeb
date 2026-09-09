package com.caochung.recruitment.scheduler;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.messaging.dto.JobAlertMessage;
import com.caochung.recruitment.messaging.publisher.JobAlertPublisher;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SCHEDULER-JOB")
public class JobAlertScheduler {
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobAlertPublisher publisher;
    private final JobMapper jobMapper;

//    @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional(readOnly = true)
    public void scheduledJobAlertEmail() {
        log.info("STARTING SCHEDULER JOB ALERT EMAIL");
        List<Job> jobs = jobRepository.findAllByActive(JobStatusEnum.OPEN);
        List<Subscriber> subscribers = subscriberRepository.findAll();

        if (subscribers.isEmpty() || jobs.isEmpty()) {
            log.info("No subscribers or active jobs found. Skipping job alert.");
            return;
        }

        int publishedCount = 0;

        for (Subscriber subscriber : subscribers) {
            List<Job> matchedJobs = jobs.stream()
                    .filter(job -> isSkillMatched(subscriber, job)).toList();
            if (!matchedJobs.isEmpty()) {
                List<JobAlertMessage.JobSummaryMessage> jobSummaries = matchedJobs.stream()
                                .map(jobMapper::toJobSummary).toList();
                JobAlertMessage message = JobAlertMessage.builder()
                        .subscriberEmail(subscriber.getEmail())
                        .subscriberName(subscriber.getName())
                        .matchedJobs(jobSummaries)
                        .build();
                publisher.publish(message);
                publishedCount++;
            }
        }
        log.info("Job alert scheduler finished successfully. Published {} job alert messages.", publishedCount);
    }

    private boolean isSkillMatched(Subscriber subscriber, Job job) {
        return !Collections.disjoint(job.getSkills(), subscriber.getSkills());
    }

}
