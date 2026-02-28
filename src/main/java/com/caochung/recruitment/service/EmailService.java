package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Job;

import java.util.List;

public interface EmailService {
    void sendJobAlertEmail(String to, String subscriberName, List<Job> jobs);
}
