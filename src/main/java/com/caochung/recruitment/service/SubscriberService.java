package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface SubscriberService {
    SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO);

    PaginationResponseDTO getAllSubscriber(Specification<Subscriber> specification, Pageable pageable);

    SubscriberResponseDTO getSubscriberById(Long id);

    void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO);

    void deleteSubscriber(Long id);
}
