package com.caochung.recruitment.listener;

import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j(topic = "AUTH-LISTENER")
@RequiredArgsConstructor
public class AuthEventListener {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegisterEvent(UserRegisterEvent user) {
        log.info("SEND EMAIL TO {}", user.getEmail());
        try {
            emailService.emailVerification(user);
            log.info("SEND EMAIL SUCCESSFULLY");
        }catch (Exception e) {
            log.error("SEND EMAIL FAILED: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleForgotPasswordEvent(ForgotPasswordEvent user) {
        log.info("SEND EMAIL TO {}", user.getEmail());
        try {
            emailService.sendForgotPasswordEmail(user);
            log.info("SEND EMAIL SUCCESSFULLY");
        }catch (Exception e) {
            log.error("SEND EMAIL FAILED: {}", e.getMessage());
        }
    }
}
