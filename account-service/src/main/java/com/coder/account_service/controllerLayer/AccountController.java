package com.coder.account_service.controllerLayer;

import com.coder.account_service.custom.ResponseVO;
import com.coder.account_service.dto.reqDto.AccountCreationRequest;
import com.coder.account_service.dto.reqDto.AmountTransferReqDto;
import com.coder.account_service.serviceLayer.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("create-account")
    public ResponseEntity<ResponseVO<?>> createAccount(@RequestBody AccountCreationRequest req){
        return accountService.createAccount(req);
    }

    @PostMapping("transfer-amount")
    public ResponseEntity<ResponseVO<Boolean>> transferAmount(@RequestBody AmountTransferReqDto req){
        return accountService.transferAmount(req);
    }

//    @GetMapping("verify-transfer/{transCode}")
//    public ResponseEntity<ResponseVO<Boolean>> verifyProcessedTransaction(@PathVariable String transCode){
//        return accountService.verifyProcessedTransaction(transCode);
//    }
}





