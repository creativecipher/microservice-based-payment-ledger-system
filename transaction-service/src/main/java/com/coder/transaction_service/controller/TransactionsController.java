package com.coder.transaction_service.controller;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.dto.reDto.TransactionsRequest;
import com.coder.transaction_service.modelLayer.model.Transactions;
import com.coder.transaction_service.serviceLayer.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("trans")
@CrossOrigin("*")
public class TransactionsController {

    private final TransactionsService transactionsService;

//    private Logger logger = LoggerFactory.getLogger(TransactionsController.class);

//    @CircuitBreaker(name="transferAmountBreaker",
//                    fallbackMethod = "transferAmountFallbackMtd")
    // move the circuit-breaker call from controller to service
    // due to Infrastructure Failures (which require a circuit breaker fallback)
    // and Business Rules Failures (which must bubble up directly to the client as an error)
    @PostMapping("create-transaction")
    ResponseEntity<ResponseVO<Transactions>> createTransaction(@RequestBody TransactionsRequest req){
        return transactionsService.createTransaction(req);
    }

//    // fallbackMethod execution: when any service is down
//    // then fallbackMethod is get executed
//    // fallbackMethod must have same return type and parameter as its calling api method
//    ResponseEntity<ResponseVO<Transactions>> transferAmountFallbackMtd(@RequestBody TransactionsRequest req,Exception e){
//        logger.error("Fallback is executed because account-service down so transaction cannot perform!");
//        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

        @GetMapping("transCode/{transCode}")
    ResponseEntity<ResponseVO<Transactions>> findByTransCode(@PathVariable String transCode){
        return transactionsService.findByTransCode(transCode);
    }
}
