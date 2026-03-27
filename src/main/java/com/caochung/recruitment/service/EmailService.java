package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.event.ForgotPasswordEvent;
import com.caochung.recruitment.event.ResumeStatusUpdateEvent;
import com.caochung.recruitment.event.UserRegisterEvent;

import java.util.List;

public interface EmailService {
    void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs);

    void emailVerification(UserRegisterEvent user);

    void sendResumeStatusEmail(ResumeStatusUpdateEvent resume);

    void sendForgotPasswordEmail(ForgotPasswordEvent user);
}
