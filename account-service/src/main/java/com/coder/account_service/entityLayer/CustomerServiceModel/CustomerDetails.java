package com.coder.account_service.dataLayer.CustomerServiceModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerDetails {
    private Long customerId;
    private String firstName;
    private String lastName;
    private Long phoneNo;
    private String emailId;
    private String password;
}

