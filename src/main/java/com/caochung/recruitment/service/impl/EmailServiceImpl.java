package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs) {
        try{
            log.info("Send email");
            Context context = new Context();
            context.setVariable("name", subscriberName);
            context.setVariable("jobs", jobs);
            String emailContent = templateEngine.process("job-alert", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject("Có "+ jobs.size()+ " việc làm mới phù hợp với bạn hôm nay!");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("Email send successful");
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", to, e.getMessage());
        }
    }
}
