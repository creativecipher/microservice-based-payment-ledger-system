package com.coder.transaction_service.serviceLayer.impl;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.custom.exceptions.BadRequestException;
import com.coder.transaction_service.custom.exceptions.RemoteInfrastructureException;
import com.coder.transaction_service.custom.exceptions.TransactionFailedException;
import com.coder.transaction_service.custom.gateways.AccountServiceGateway;
import com.coder.transaction_service.custom.helper.TransactionInternalOps;
import com.coder.transaction_service.dataLayer.TransactionsRepository;
import com.coder.transaction_service.dto.reDto.AmountTransferReqDto;
import com.coder.transaction_service.dto.reDto.TransactionsRequest;
import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import com.coder.transaction_service.modelLayer.enums.TransactionType;
import com.coder.transaction_service.modelLayer.model.Transactions;
import com.coder.transaction_service.serviceLayer.TransactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionsServiceImpl implements TransactionsService {

    private final TransactionsRepository transactionsRepository;

//    private final Logger logger = LoggerFactory.getLogger(TransactionsServiceImpl.class);

//    private final RestTemplate restTemplate;

    // cannot add final keyword
    // -> as we cannot use @RequiredArgsConstructor with @Value
//    @Value("${account.service.url}")
//    private String ACCOUNT_SERVICE_URL;

    private final AccountServiceGateway accountServiceGateway;

    private final TransactionInternalOps transactionInternalOps;

    public ResponseEntity<ResponseVO<Transactions>> createTransaction(TransactionsRequest req) {

        TransactionType transactionType = transactionInternalOps.validateTransactionRequest(req);

        String transCode = req.getTransCode()==null || req.getTransCode().trim().isEmpty()
                ? UUID.randomUUID().toString()
                : req.getTransCode().trim();

        Optional<Transactions> existingTransactions = transactionsRepository.findByTransCode(transCode);
        if(existingTransactions.isPresent()){
            log.info("Idempotency key hit detected for transCode: {}, Processing transparent playback.",transCode);
            return handleIdempotentPlayback(existingTransactions.get());
        }

        // 1. Pre-build the transaction log to guarantee we don't lose the record on unexpected errors
//        Transactions transaction = new Transactions();
//        transaction.setTransCode(transCode);
//        transaction.setFromAccount(req.getFromAccount());
//        transaction.setToAccount(req.getToAccount());
//        transaction.setAmount(req.getAmount());
//        transaction.setTransType(transactionType);
//        transaction.setTransCreatedAt(LocalDateTime.now());
//        transaction.setTransStatus(TransactionStatus.INITIALED);
//        transaction = transactionsRepository.save(transaction);

        Transactions transaction = transactionInternalOps.initializaLedgerRecord(transCode,req,transactionType);

        AmountTransferReqDto amountTransferReqDto = AmountTransferReqDto
                .builder()
                .amount(req.getAmount())
                .fromAccount(req.getFromAccount())
                .toAccount(req.getToAccount())
                .transCode(transCode)
                .build();
        try{
            ResponseEntity<ResponseVO<Boolean>> response = accountServiceGateway.executeRemoteTransfer(amountTransferReqDto);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody()!=null){
                ResponseVO<Boolean> res = response.getBody();
                if(res.getStatusCode()==HttpStatus.OK.value() && Boolean.TRUE.equals(res.getResult())){
                    // STEP 3A: Finalize SUCCESS in an isolated transact
                    transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(), TransactionStatus.SUCCESS, "Amount is transferred successfully!", HttpStatus.CREATED);

                    ResponseVO<Transactions> successVo = new ResponseVO<>();
                    successVo.setStatusCode(HttpStatus.CREATED.value());
                    successVo.setMsg("Amount transferred successfully!");
                    successVo.setResult(transaction);
                    return new ResponseEntity<>(successVo, HttpStatus.CREATED);
                }else{
                    log.error("Error occurred while transferring amount for tranCode {} : {}",transCode, res.getMsg());
                    transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(),TransactionStatus.FAILED,"Transfer rejected: "+res.getMsg(),HttpStatus.UNPROCESSABLE_ENTITY);

                    ResponseVO<Transactions> failVo = new ResponseVO<>();
                    failVo.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
                    failVo.setMsg("Transfer rejected: " + res.getMsg());
                    failVo.setResult(transaction);
                    return new ResponseEntity<>(failVo, HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }

            // Fallback safety catch-all for weird non-2xx scenarios not managed by the gateway
            // Fixed: Explicitly handle non-2xx raw payloads that slipped through standard Gateway filter limits
            log.warn("Account service returned unmanaged non-2xx status code {} for transCode: {}", response.getStatusCode(), transCode);
            transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(), TransactionStatus.SUSPENDED, "Received unmanaged non-2xx status infrastructure response", HttpStatus.ACCEPTED);
            return transactionInternalOps.handleUncertainStateByCode(transCode);
        }catch(RemoteInfrastructureException e){
            log.error("NETWORK TIMEOUT / DOWNSTREAM OUTAGE DETECTED for transCode: {}. Moving to SUSPENDED statUS.", transCode, e);
            // Explicitly update db row state to SUSPENDED to prevent connection leaks
            transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(),TransactionStatus.SUSPENDED,"Infrastructure failure during execution",HttpStatus.ACCEPTED);
            throw e;
        }catch(HttpClientErrorException e){
            log.error("Client side exception (HTTP 4xx) from Account Service for transCode: {}", transCode, e);
            transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(),TransactionStatus.FAILED,"Account service rejected payload",HttpStatus.BAD_REQUEST);
            throw new BadRequestException("Account service rejected request payload: " + e.getResponseBodyAsString());
        }catch (TransactionFailedException e) {
            throw e; // Re-throw business exceptions clean
        } catch (Exception e) {
            // Catch-all for unexpected local exceptions (like JSON parsing glitches)
            log.error("Unexpected exception occurred while transferring amount for transCode: {}", transCode, e);

            // FIX: Force update status to SUSPENDED to prevent data locking on local software anomalies
            transaction = transactionInternalOps.finalizeLedgerRecord(transaction.getTrasId(), TransactionStatus.SUSPENDED, "Internal system error processing transaction", HttpStatus.ACCEPTED);
            return transactionInternalOps.handleUncertainStateByCode(transCode);
        }
    }

    @Override
    public ResponseEntity<ResponseVO<Transactions>> findByTransCode(String transCode) {

        if(transCode==null || transCode.trim().isEmpty()){
            throw new BadRequestException("Requested Transaction Code should not be empty!");
        }
        return transactionsRepository.findByTransCode(transCode)
                .map(transaction -> {
                    ResponseVO<Transactions> responseVO = new ResponseVO<>();
                    responseVO.setResult(transaction);
                    responseVO.setMsg("Transaction is found!");
                    responseVO.setStatusCode(HttpStatus.OK.value());
                    return new ResponseEntity<>(responseVO,HttpStatus.OK);
                })
                .orElseGet(() -> {
                    ResponseVO<Transactions> responseVO = new ResponseVO<>();
                    responseVO.setMsg("Transaction not found!");
                    responseVO.setStatusCode(HttpStatus.NOT_FOUND.value());
                    return new ResponseEntity<>(responseVO,HttpStatus.NOT_FOUND);
                });
    }

    private ResponseEntity<ResponseVO<Transactions>> handleIdempotentPlayback(Transactions existingTransaction){
        ResponseVO<Transactions> responseVO = new ResponseVO<>();
        switch(existingTransaction.getTransStatus()){
            case SUCCESS :
                responseVO.setStatusCode(HttpStatus.CREATED.value());
                responseVO.setMsg("Amount is transfer successfully! (Replayed Entry)");
                responseVO.setResult(existingTransaction);
                return new ResponseEntity<>(responseVO,HttpStatus.CREATED);

            case FAILED:
                responseVO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                responseVO.setMsg("Amount is failed to transfer! (Replayed Entry)");
                responseVO.setResult(existingTransaction);
                return new ResponseEntity<>(responseVO,HttpStatus.INTERNAL_SERVER_ERROR);

            case SUSPENDED:
                responseVO.setStatusCode(HttpStatus.ACCEPTED.value());
                responseVO.setMsg("Transaction status is uncertain due to a network or server error. It will be reconciled shortly. (Replayed Entry)");
                responseVO.setResult(existingTransaction);
                return new ResponseEntity<>(responseVO,HttpStatus.ACCEPTED);

            case INITIALED:
            default: throw new BadRequestException("A request processing loop is already active for transaction reference: " + existingTransaction.getTransCode());
        }
    }


}
