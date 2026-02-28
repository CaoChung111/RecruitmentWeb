package com.caochung.recruitment.service;

import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;

public interface SubscriberService {
    SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO);

    SubscriberResponseDTO getSubscriberById(Long id);

    void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO);

    void deleteSubscriber(Long id);
}
