package com.coder.customer_service.dto.reqDto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerRegistrationRequest {
    private String firstName;
    private String lastName;
    private Long phoneNo;
    private String emailId;
    private String password;
}
