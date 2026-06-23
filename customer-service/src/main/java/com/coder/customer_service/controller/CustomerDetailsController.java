package com.coder.customer_service.controller;

import com.coder.customer_service.custom.ResponseVO;
import com.coder.customer_service.dto.reqDto.CustomerRegistrationRequest;
import com.coder.customer_service.dto.resDto.CustomerResponse;
import com.coder.customer_service.service.CustomerDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
@CrossOrigin("*")
public class CustomerDetailsController {

    private final CustomerDetailsService customerDetailsService;

    // Spring MVC controller are singleton by default
    // means exactly one instance of this controller is shared to all incoming HTTP request
    // IF User A and User B both register exact same millisecon then they overwriten the shared responseVO field ,
    // causing User A to get User B's confidential details
    //private ResponseVO responseVO;

    @PostMapping("register-customer")
    ResponseEntity<ResponseVO<?>> registerCustomer(@RequestBody CustomerRegistrationRequest reqDto){
        Boolean customerResponse = customerDetailsService.registerCustomer(reqDto);
        if(customerResponse==true){
            ResponseVO responseVO = new ResponseVO();
            responseVO.setStatusCode(HttpStatus.CREATED.value());
            responseVO.setMsg("Customer register successfully!");
            responseVO.setResult(reqDto);

            return new ResponseEntity<>(responseVO,HttpStatus.CREATED);
        }
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseVO.setMsg("Customer registeration failed!");
        responseVO.setResult(reqDto);
        return new ResponseEntity<>(responseVO,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("{customerId}")
    ResponseEntity<ResponseVO<?>> getCustomerById(@PathVariable Long customerId){
        CustomerResponse customerResponse = customerDetailsService.getCustomerById(customerId);
        if(customerResponse!=null){
            ResponseVO responseVO = new ResponseVO();
            responseVO.setStatusCode(HttpStatus.OK.value());
            responseVO.setMsg("Customer Details found!");
            responseVO.setResult(customerResponse);
            return new ResponseEntity<>(responseVO,HttpStatus.OK);
        }
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatusCode(HttpStatus.NOT_FOUND.value());
        responseVO.setMsg("Customer Details not found with id "+customerId);
        responseVO.setResult(customerId);
        return new ResponseEntity<>(responseVO,HttpStatus.NOT_FOUND);
    }

}




