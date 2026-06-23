package com.coder.account_service.dto.reqDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AmountTransferReqDto {
    private BigDecimal amount;
    private Long fromAccount;
    private Long toAccount;
    private String transCode;
}
