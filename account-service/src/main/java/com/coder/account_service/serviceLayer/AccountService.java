package com.coder.account_service.serviceLayer;

import com.coder.account_service.custom.ResponseVO;
import com.coder.account_service.dto.reqDto.AccountCreationRequest;
import com.coder.account_service.dto.reqDto.AmountTransferReqDto;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface AccountService {
    public ResponseEntity createAccount(AccountCreationRequest req);
    public ResponseEntity<ResponseVO<Boolean>> transferAmount(AmountTransferReqDto req);

    ResponseEntity<ResponseVO<Boolean>> verifyProcessedTransaction(String transCode);
}
