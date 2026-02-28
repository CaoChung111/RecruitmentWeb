package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.SubscriberService;
import com.caochung.recruitment.service.mapper.SubscriberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriberServiceImpl implements SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SubscriberMapper subscriberMapper;

    @Override
    public SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO) {
        if(subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        Subscriber subscriber = subscriberMapper.toEntity(subscriberRequestDTO);
        Subscriber savedSubscriber = this.subscriberRepository.save(subscriber);
        return subscriberMapper.toDTO(savedSubscriber);
    }

    @Override
    public SubscriberResponseDTO getSubscriberById(Long id) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        return subscriberMapper.toDTO(subscriber);
    }

    @Override
    public void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        if(!subscriber.getEmail().equals(subscriberRequestDTO.getEmail())
                && subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        subscriberMapper.fromUpdate(subscriberRequestDTO, subscriber);
        subscriberRepository.save(subscriber);
    }

    @Override
    public void deleteSubscriber(Long id) {
        this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        this.subscriberRepository.deleteById(id);
    }
}
