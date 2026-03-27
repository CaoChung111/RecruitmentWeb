package com.caochung.recruitment.listener;

import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j(topic = "RESUME-LISTENER")
@RequiredArgsConstructor
public class ResumeEventListener {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeStatusUpdateEvent(ResumeStatusUpdateEvent resumeStatusUpdateEvent) {
        log.info("SEND EMAIL TO {}", resumeStatusUpdateEvent.getEmailTo());
        try {
            emailService.sendResumeStatusEmail(resumeStatusUpdateEvent);
        }catch (Exception e){
            log.error("ERROR SEND EMAIL UPDATE STATUS RESUME: {}",e.getMessage());
        }
    }
}
