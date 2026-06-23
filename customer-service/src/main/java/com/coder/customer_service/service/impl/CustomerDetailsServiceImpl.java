package com.coder.customer_service.service.impl;

import com.coder.customer_service.custom.exceptions.ResourceNotFoundException;
import com.coder.customer_service.dto.reqDto.CustomerRegistrationRequest;
import com.coder.customer_service.dto.resDto.CustomerResponse;
import com.coder.customer_service.model.CustomerDetails;
import com.coder.customer_service.repository.CustomerDetailsRepository;
import com.coder.customer_service.service.CustomerDetailsService;
import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDetailsServiceImpl implements CustomerDetailsService {

    private final CustomerDetailsRepository customerDetailsRepository;

    private final PasswordEncoder passwordEncoder;

  //  private Logger logger = LoggerFactory.getLogger(CustomerDetailsServiceImpl.class);
    @Override
    public Boolean registerCustomer(CustomerRegistrationRequest reqDto) {

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setFirstName(reqDto.getFirstName());
        customerDetails.setLastName(reqDto.getLastName());
        customerDetails.setPhoneNo(reqDto.getPhoneNo());
        String pass = encryptPassword(reqDto.getPassword());
        customerDetails.setPassword(pass);
        customerDetails.setEmailId(reqDto.getEmailId());
        customerDetails =  customerDetailsRepository.save(customerDetails);
        return true;
     //   return "Customer register successfully!";

      //  return "Customer registeration failed!";
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {

        CustomerDetails customerDetails = customerDetailsRepository.findById(customerId)
                .orElseThrow(()->new ResourceNotFoundException("Customer Details not found with id "+customerId));
        return CustomerResponse
                .builder()
                .customerId(customerDetails.getCustomerId())
                .firstName(customerDetails.getFirstName())
                .lastName(customerDetails.getLastName())
                .phoneNo(customerDetails.getPhoneNo())
                .emailId(customerDetails.getEmailId())
                .msg("Customer Details found!")
                .build();
    }

    private String encryptPassword(String password){
        if(password==null || password.trim().isEmpty()){
            password = "Test@123";
        }
        return passwordEncoder.encode(password);
    }
}



