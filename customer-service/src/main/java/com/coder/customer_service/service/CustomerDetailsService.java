package com.coder.customer_service.service;

import com.coder.customer_service.dto.reqDto.CustomerRegistrationRequest;
import com.coder.customer_service.dto.resDto.CustomerResponse;

public interface CustomerDetailsService {
     Boolean registerCustomer(CustomerRegistrationRequest reqDto);

     CustomerResponse getCustomerById(Long customerId);
}
