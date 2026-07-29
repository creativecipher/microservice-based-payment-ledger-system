package com.coder.customer_service.service.impl;

import com.coder.customer_service.apiSecurityPkg.util.JwtGeneratorUtil;
import com.coder.customer_service.custom.exceptions.BadRequestException;
import com.coder.customer_service.custom.exceptions.ResourceNotFoundException;
import com.coder.customer_service.custom.exceptions.UnauthorizedException;
import com.coder.customer_service.dto.reqDto.CustomerRegistrationRequest;
import com.coder.customer_service.dto.reqDto.LoginDto;
import com.coder.customer_service.dto.resDto.CustomerResponse;
import com.coder.customer_service.model.CustomerDetails;
import com.coder.customer_service.repository.CustomerDetailsRepository;
import com.coder.customer_service.service.CustomerDetailsService;
import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerDetailsServiceImpl implements CustomerDetailsService {

    private final CustomerDetailsRepository customerDetailsRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtGeneratorUtil jwtGeneratorUtil;

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
    public String loginCustomer(LoginDto reqDto) {
        if(reqDto==null || reqDto.getEmailId()==null || reqDto.getEmailId().trim().isEmpty()
                || reqDto.getPassword()==null || reqDto.getPassword().trim().isEmpty()){
            throw new BadRequestException("Invalid Input fields");
        }
        authenticateUser(reqDto.getEmailId(),reqDto.getPassword());
        return generateTokenFromUsername(reqDto.getEmailId());
    }

    private void authenticateUser(String emailId,String password){
            Optional<CustomerDetails> customerDetails = customerDetailsRepository.findByEmailId(emailId);
            if(customerDetails.isEmpty()){
                throw new BadRequestException("Customer not Present!");
            }
            if(!passwordEncoder.matches(password, customerDetails.get().getPassword())){
                throw new UnauthorizedException("Invalid Customer Credentials!");
            }
    }

    private String generateTokenFromUsername(String username){
        return jwtGeneratorUtil.generateTokenFromUserName(username);
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



