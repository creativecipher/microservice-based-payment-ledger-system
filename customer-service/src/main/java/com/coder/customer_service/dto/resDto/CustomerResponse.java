package com.coder.customer_service.dto.resDto;

import jakarta.persistence.Column;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerResponse {
    private Long customerId;
    private String firstName;
    private String lastName;
    private Long phoneNo;
    private String emailId;
    private String msg;
}
