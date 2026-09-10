package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;
import com.caochung.recruitment.messaging.dto.JobAlertMessage;
import com.caochung.recruitment.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendJobAlertEmail(String to, String subscriberName, List<JobAlertMessage.JobSummaryMessage> jobs) {
        log.info("SEND JOB ALERT EMAIL TO {}", to);
        Context context = new Context();
        context.setVariable("name", subscriberName);
        context.setVariable("jobs", jobs);
        sendEmail(to, "Có " + jobs.size() + " việc làm mới phù hợp với bạn hôm nay!",
                templateEngine.process("job-alert", context));
    }


    @Override
    public void emailVerification(UserRegisterEvent user) {
        log.info("SEND EMAIL VERIFICATION TO {}", user.getEmail());
        Context context = new Context();
        context.setVariable("otpCode", user.getOtpToken());
        sendEmail(user.getEmail(), "Account verification",
                templateEngine.process("email-verification", context));
    }

    @Override
    public void sendResumeStatusEmail(ResumeStatusUpdateEvent resume){
        log.info("SEND RESUME STATUS EMAIL TO {}", resume.getEmailTo());
        Context context = new Context();
        context.setVariable("resume", resume);
        sendEmail(resume.getEmailTo(), "Resume status update",
                templateEngine.process("resume-status-update", context));

    }

    @Override
    public void sendForgotPasswordEmail(ForgotPasswordEvent user){
        log.info("SEND FORGOT PASSWORD EMAIL TO {}", user.getEmail());
        Context context = new Context();
        context.setVariable("otpCode", user.getOtpToken());
        sendEmail(user.getEmail(), "Forgot password",
                templateEngine.process("forgot-password", context));
    }

    @Override
    public void sendNotificationEmail(EmailNotificationMessage message) {
        log.info("PROCESSING NOTIFICATION EMAIL: TYPE={}, TO={}", message.getNotificationType(), message.getEmailTo());
        if (message.getOtpToken() != null && !message.getOtpToken().isBlank()) {
            log.info(">>> [OTP NOTIFICATION] To: {}, Type: {}, OTP Code: [{}]", message.getEmailTo(), message.getNotificationType(), message.getOtpToken());
        }
        switch (message.getNotificationType()) {
            case USER_REGISTER -> {
                Context context = new Context();
                context.setVariable("otpCode", message.getOtpToken());
                sendEmail(message.getEmailTo(), "Account verification",
                        templateEngine.process("email-verification", context));
            }
            case FORGOT_PASSWORD ->  {
                Context context = new Context();
                context.setVariable("otpCode", message.getOtpToken());
                sendEmail(message.getEmailTo(), "Forgot password",
                        templateEngine.process("forgot-password", context));
            }
            case RESUME_STATUS_UPDATE ->  {
                Context context = new Context();
                context.setVariable("resume", message);
                sendEmail(message.getEmailTo(), "Resume status update",
                        templateEngine.process("resume-status-update", context));
            }
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("EMAIL SEND SUCCESSFUL TO {} : {}", to, subject);
        } catch (Exception e) {
            log.error("ERROR SEND EMAIL TO {} : {}", to, e.getMessage());
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

}
