package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.service.SubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriberController {
    private final SubscriberService subscriberService;

    @PostMapping("/subscribers")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_CREATE)
    public ResponseData<SubscriberResponseDTO> createSubscriber(@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.createSubscriber(subscriberRequestDTO);
        return ResponseData.success(subscriberResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_VIEW_DETAIL)
    public ResponseData<SubscriberResponseDTO> getSubscriberById(@PathVariable Long id){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.getSubscriberById(id);
        return ResponseData.success(subscriberResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_UPDATE)
    public ResponseData<?> updateSubscriber(@PathVariable Long id ,@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        subscriberService.updateSubscriber(id, subscriberRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_DELETE)
    public ResponseData<?> deleteSubscriber(@PathVariable Long id){
        subscriberService.deleteSubscriber(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
