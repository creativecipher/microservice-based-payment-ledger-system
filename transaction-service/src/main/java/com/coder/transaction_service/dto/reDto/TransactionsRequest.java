package com.coder.transaction_service.dto.reDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionsRequest {
    private String transCode;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private String transType;
}
