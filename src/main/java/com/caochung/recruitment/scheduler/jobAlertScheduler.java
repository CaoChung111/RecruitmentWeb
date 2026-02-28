package com.caochung.recruitment.scheduler;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class jobAlertScheduler {
    private final JobRepository jobRepository;
    private final EmailService emailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void scheduledJobAlertEmail() {
        log.info("Scheduling job alert email");
        List<Job> jobs = jobRepository.findAll();
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for (Subscriber subscriber : subscribers) {
            List<Job> matchedJobs = jobs.stream()
                    .filter(job -> isSkillMatched(subscriber, job)).toList();
            if (!matchedJobs.isEmpty()) {
                emailService.sendJobAlertEmail(subscriber.getEmail(), subscriber.getName(), matchedJobs);
            }
        }
        log.info(">>> Kết thúc quét Job. Các email đang được gửi ngầm (Async).");
    }

    private boolean isSkillMatched(Subscriber subscriber, Job job) {
        return !Collections.disjoint(job.getSkills(), subscriber.getSkills());
    }
}
