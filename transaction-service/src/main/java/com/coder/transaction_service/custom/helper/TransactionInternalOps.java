package com.coder.transaction_service.custom.helper;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.custom.exceptions.BadRequestException;
import com.coder.transaction_service.dataLayer.TransactionsRepository;
import com.coder.transaction_service.dto.reDto.TransactionsRequest;
import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import com.coder.transaction_service.modelLayer.enums.TransactionType;
import com.coder.transaction_service.modelLayer.model.Transactions;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Log4j2
public class TransactionInternalOps {
    private final TransactionsRepository transactionsRepository;

    public TransactionType validateTransactionRequest(TransactionsRequest req){
        if(req==null || req.getFromAccount()==null || req.getToAccount()==null || req.getAmount()==null || req.getTransType()==null || req.getTransType().trim().isEmpty()){
            throw new BadRequestException("Requested Transaction should not be empty!");
        }
        if( req.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new BadRequestException("Invalid amount provided for transfer");
        }
        return TransactionType.TRANSFER;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transactions initializaLedgerRecord(String transCode, TransactionsRequest req, TransactionType transactionType) {
        Transactions transaction = new Transactions();
        transaction.setTransCode(transCode);
        transaction.setFromAccount(req.getFromAccount());
        transaction.setToAccount(req.getToAccount());
        transaction.setAmount(req.getAmount());
        transaction.setTransType(transactionType);
        transaction.setTransCreatedAt(LocalDateTime.now());
        transaction.setTransStatus(TransactionStatus.INITIALED);
        return transactionsRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transactions finalizeLedgerRecord(Long trasId, TransactionStatus status, String msg, HttpStatus httpStatus) {
        return transactionsRepository.findById(trasId)
                .map(tx -> {
                    tx.setTransStatus(status);
                    return transactionsRepository.save(tx);
                }).orElseThrow(() -> new IllegalStateException("Critical error: Initialized record disappeared during state resolution"));

    }

    public ResponseEntity<ResponseVO<Transactions>> handleUncertainStateByCode(String transCode) {
        Transactions transaction = transactionsRepository.findByTransCode(transCode)
                .orElseThrow(() -> new IllegalStateException("Ledger reference missing for code: " + transCode));

        ResponseVO<Transactions> responseVO = new ResponseVO<>();
        responseVO.setStatusCode(HttpStatus.ACCEPTED.value());
        responseVO.setMsg("Transaction status is uncertain due to a network or server error. It will be reconciled shortly.");
        responseVO.setResult(transaction);
        return new ResponseEntity<>(responseVO, HttpStatus.ACCEPTED);
    }
}
