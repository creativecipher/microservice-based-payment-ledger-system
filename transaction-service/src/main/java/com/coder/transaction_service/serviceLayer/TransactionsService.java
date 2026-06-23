package com.coder.transaction_service.serviceLayer;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.dto.reDto.TransactionsRequest;
import com.coder.transaction_service.modelLayer.model.Transactions;
import org.springframework.http.ResponseEntity;

public interface TransactionsService {
    ResponseEntity createTransaction(TransactionsRequest req);

    ResponseEntity<ResponseVO<Transactions>> findByTransCode(String transCode);
}
