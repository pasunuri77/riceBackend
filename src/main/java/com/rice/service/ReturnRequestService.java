package com.rice.service;

import com.rice.dto.returnrequest.*;
import com.rice.entity.User;

import java.util.List;

public interface ReturnRequestService {
    ReturnableItemsResponseDto getReturnableItems(Long orderId, Long customerId);
    
    ReturnRequestResponseDto submitReturnRequest(ReturnRequestSubmissionDto dto, User customer);
    
    List<ReturnRequestResponseDto> getCustomerReturnRequests(Long customerId);
    
    List<ReturnRequestResponseDto> getAllReturnRequests(String status);
    
    ReturnRequestResponseDto getReturnRequestByIdForCustomer(Long id, Long customerId);
    
    ReturnRequestResponseDto getReturnRequestByIdForAdmin(Long id);
    
    ReturnRequestResponseDto approveReturnRequest(Long id, ApproveReturnDto dto);
    
    ReturnRequestResponseDto rejectReturnRequest(Long id, RejectReturnDto dto);
}
