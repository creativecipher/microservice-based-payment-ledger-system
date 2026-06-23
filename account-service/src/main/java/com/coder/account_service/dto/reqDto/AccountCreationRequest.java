package com.coder.account_service.dto.reqDto;

import com.coder.account_service.dataLayer.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountCreationRequest {
    private Long customerId;
    private Long bankId;
    private AccountType accountType;
    private BigDecimal balance;
}
