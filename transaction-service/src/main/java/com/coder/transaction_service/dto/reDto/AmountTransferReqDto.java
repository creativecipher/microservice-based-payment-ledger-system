package com.coder.transaction_service.dto.reDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
public class AmountTransferReqDto {
    private BigDecimal amount;
    private Long fromAccount;
    private Long toAccount;
    private String transCode;
}


