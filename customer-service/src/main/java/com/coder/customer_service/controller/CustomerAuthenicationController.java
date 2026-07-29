package com.coder.customer_service.controller;

import com.coder.customer_service.custom.ResponseVO;
import com.coder.customer_service.dto.reqDto.CustomerRegistrationRequest;
import com.coder.customer_service.dto.reqDto.LoginDto;
import com.coder.customer_service.service.CustomerDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer-auth")
@CrossOrigin("*")
public class CustomerAuthenicationController {

    private final CustomerDetailsService customerDetailsService;

    @PostMapping("login")
    ResponseEntity<ResponseVO<String>> loginCustomer(@RequestBody LoginDto reqDto){
        String customerResponse = customerDetailsService.loginCustomer(reqDto);

        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatusCode(HttpStatus.OK.value());
        responseVO.setMsg("Customer login successfully!");
        responseVO.setResult(customerResponse);
        return new ResponseEntity<>(responseVO,HttpStatus.OK);
    }
}
