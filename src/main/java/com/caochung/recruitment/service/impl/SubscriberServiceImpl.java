package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.SubscriberRepository;
import com.caochung.recruitment.service.SubscriberService;
import com.caochung.recruitment.service.mapper.SubscriberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriberServiceImpl implements SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final SubscriberMapper subscriberMapper;

    @Override
    @Transactional
    public SubscriberResponseDTO createSubscriber(SubscriberRequestDTO subscriberRequestDTO) {
        if(subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        Subscriber subscriber = subscriberMapper.toEntity(subscriberRequestDTO);
        Subscriber savedSubscriber = this.subscriberRepository.save(subscriber);
        return subscriberMapper.toDTO(savedSubscriber);
    }

    @Override
    public PaginationResponseDTO getAllSubscriber(Specification<Subscriber> specification, Pageable pageable) {
        Page<Subscriber> pageSubscribers = this.subscriberRepository.findAll(specification, pageable);
        List<SubscriberResponseDTO> responseDTOs = subscriberMapper.toDTO(pageSubscribers.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageSubscribers.getTotalPages())
                .totalItems(pageSubscribers.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Override
    public SubscriberResponseDTO getSubscriberById(Long id) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        return subscriberMapper.toDTO(subscriber);
    }

    @Override
    @Transactional
    public void updateSubscriber(Long id, SubscriberRequestDTO subscriberRequestDTO) {
        Subscriber subscriber = this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        if(!subscriber.getEmail().equals(subscriberRequestDTO.getEmail())
                && subscriberRepository.existsByEmail(subscriberRequestDTO.getEmail())){
            throw new AppException(ErrorCode.SUBSCRIBER_EXISTED);
        }
        subscriberMapper.fromUpdate(subscriberRequestDTO, subscriber);
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long id) {
        this.subscriberRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND));
        this.subscriberRepository.deleteById(id);
    }
}
