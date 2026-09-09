package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;
import com.caochung.recruitment.messaging.dto.EmailNotificationMessage;
import com.caochung.recruitment.messaging.dto.JobAlertMessage;

import java.util.List;

public interface EmailService {

    void sendJobAlertEmail(String to, String subscriberName, List<JobAlertMessage.JobSummaryMessage> jobs);

    void emailVerification(UserRegisterEvent user);

    void sendResumeStatusEmail(ResumeStatusUpdateEvent resume);

    void sendForgotPasswordEmail(ForgotPasswordEvent user);

    void sendNotificationEmail(EmailNotificationMessage message);

}
