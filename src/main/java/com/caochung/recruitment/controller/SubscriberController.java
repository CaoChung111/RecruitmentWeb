package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Subscriber;
import com.caochung.recruitment.dto.request.SubscriberRequestDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.SubscriberResponseDTO;
import com.caochung.recruitment.service.SubscriberService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Subscriber Management", description = "APIs for managing email subscribers and notifications.")
public class SubscriberController {
    private final SubscriberService subscriberService;

    @Operation(summary = "Create a new subscriber", description = "Registers a new email subscriber. Requires 'SUBSCRIBER_CREATE' permission.")
    @PostMapping("/subscribers")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_CREATE)
    public ResponseData<SubscriberResponseDTO> createSubscriber(@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.createSubscriber(subscriberRequestDTO);
        return ResponseData.success(subscriberResponseDTO ,SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get all subscribers", description = "Retrieves a paginated list of all subscribers, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("/subscribers")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_VIEW_ALL)
    public ResponseData<PaginationResponseDTO> getAllSubscribers(
            @Filter Specification<Subscriber> specification,
            Pageable pageable) {

        PaginationResponseDTO resultPagination= this.subscriberService.getAllSubscriber(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get subscriber by ID", description = "Fetches detailed information for a specific subscriber using their unique identifier. Requires 'SUBSCRIBER_VIEW_DETAIL' permission.")
    @GetMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_VIEW_DETAIL)
    public ResponseData<SubscriberResponseDTO> getSubscriberById(@PathVariable Long id){
        SubscriberResponseDTO subscriberResponseDTO = subscriberService.getSubscriberById(id);
        return ResponseData.success(subscriberResponseDTO, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing subscriber", description = "Modifies the details of an existing subscriber identified by their ID. Requires 'SUBSCRIBER_UPDATE' permission.")
    @PutMapping("/subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_UPDATE)
    public ResponseData<?> updateSubscriber(@PathVariable Long id ,@Valid @RequestBody SubscriberRequestDTO subscriberRequestDTO){
        subscriberService.updateSubscriber(id, subscriberRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a subscriber", description = "Removes a subscriber permanently using their unique identifier. Requires 'SUBSCRIBER_DELETE' permission.")
    @DeleteMapping("subscribers/{id}")
    @PreAuthorize(SecurityConstant.SUBSCRIBER_DELETE)
    public ResponseData<?> deleteSubscriber(@PathVariable Long id){
        subscriberService.deleteSubscriber(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}
