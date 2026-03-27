package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
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
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs) {
        try{
            log.info("SEND JOB ALERT EMAIL TO {}", to);
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

    @Override
    public void emailVerification(UserRegisterEvent user) {
        try{
            log.info("SEND EMAIL VERIFICATION TO {}", user.getEmail());
            Context context = new Context();
            context.setVariable("otpCode", user.getOtpToken());
            String emailContent = templateEngine.process("email-verification", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject("Account verification");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendResumeStatusEmail(ResumeStatusUpdateEvent resume){
        try{
            log.info("SEND RESUME STATUS EMAIL TO {}", resume.getEmailTo());
            Context context = new Context();
            context.setVariable("resume", resume);
            String emailContent = templateEngine.process("resume-status-update", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(resume.getEmailTo());
            helper.setSubject("Resume status update");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("EMAIL SEND SUCCESSFUL");
        } catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", resume.getEmailTo(), e.getMessage());
        }
    }

    @Override
    public void sendForgotPasswordEmail(ForgotPasswordEvent user){
        try{
            log.info("SEND FORGOT PASSWORD EMAIL TO {}", user.getEmail());
            Context context = new Context();
            context.setVariable("otpCode", user.getOtpToken());
            String emailContent = templateEngine.process("forgot-password", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject("Forgot Password");
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
        }catch (MessagingException e) {
            log.error("ERROR SEND EMAIL TO {} : {}", user.getEmail(), e.getMessage());
        }
    }
}
