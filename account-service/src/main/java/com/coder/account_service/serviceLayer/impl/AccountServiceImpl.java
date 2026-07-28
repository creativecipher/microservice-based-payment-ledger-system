package com.coder.account_service.serviceLayer.impl;

import com.coder.account_service.custom.ResponseVO;
import com.coder.account_service.custom.exceptions.BadRequestException;
import com.coder.account_service.custom.exceptions.ResourceNotFoundException;
import com.coder.account_service.entityLayer.CustomerServiceModel.CustomerDetails;
import com.coder.account_service.entityLayer.enums.AccountStatus;
import com.coder.account_service.entityLayer.enums.EntryType;
import com.coder.account_service.entityLayer.model.BankDetails;
import com.coder.account_service.entityLayer.model.CustomerAccount;
import com.coder.account_service.entityLayer.model.LedgerAudit;
import com.coder.account_service.entityLayer.model.ProcessedTransaction;
import com.coder.account_service.dto.reqDto.AccountCreationRequest;
import com.coder.account_service.dto.reqDto.AmountTransferReqDto;
import com.coder.account_service.repository.AccountRepository;
import com.coder.account_service.repository.BankRepository;
import com.coder.account_service.repository.LedgerAuditRepository;
import com.coder.account_service.repository.ProcessedTransactionRepository;
import com.coder.account_service.serviceLayer.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final BankRepository bankRepository;

    private final RestTemplate restTemplate;

    private final ProcessedTransactionRepository processedTransactionRepository;

    private final LedgerAuditRepository ledgerAuditRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    // @Value cannot inject into static field
    // Spring Dependency Injection container injects values into instance fields when instantiating the bean component
    // Static fields belong to the class object itself rather than a specific bean instance managed by Spring
    @Value("${customer.service.url}")
    private String CUSTOMER_DETAILS_URL ;

    @Value("${bank.security.max-transfer-limit}")
    private BigDecimal MAX_LIMIT_TRANSFER;

    @Override
    public ResponseEntity createAccount(AccountCreationRequest req) {

        if(req==null) throw new RuntimeException("Request Body Not Found");
        CustomerDetails customerDetails = null;

//        try {
//            // The below line of code can throw java.lang.ClassCastException
//            ResponseEntity response = restTemplate.getForEntity(
//                    CUSTOMER_DETAILS_URL+req.getCustomerId(),
//                    ResponseEntity.class
//            );
//            if(response.getStatusCode().is2xxSuccessful()){
//                ResponseVO res = (ResponseVO) response.getBody();
//                if(res.getStatusCode()== HttpStatus.OK.value()){
//                    customerDetails = (CustomerDetails) res.getResult();
//                }else{
//                    logger.error("Error occur while fetching customer details as "+res.getMsg());
//                    ResponseVO responseVO = new ResponseVO();
//                    responseVO.setStatusCode(HttpStatus.NOT_FOUND.value());
//                    responseVO.setMsg("Error occur while fetching customer details as "+res.getMsg());
//                    return new ResponseEntity<>(responseVO,HttpStatus.NOT_FOUND);
//                }
//            }else{
//                logger.error("Error occur while fetching customer details with status code as "+response.getStatusCode());
//                ResponseVO responseVO = new ResponseVO();
//                responseVO.setStatusCode(response.getStatusCode().value());
//                responseVO.setMsg("Error occur while fetching customer details with status code as "+response.getStatusCode());
//                return new ResponseEntity<>(responseVO,HttpStatus.NOT_FOUND);
//            }
//        }catch (Exception e){
//            logger.error("Error occur while fetching customer details with status code as "+e.getMessage());
//            ResponseVO responseVO = new ResponseVO();
//            responseVO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            responseVO.setMsg("Error occur while fetching customer details as "+e.getMessage());
//            return new ResponseEntity<>(responseVO,HttpStatus.INTERNAL_SERVER_ERROR);
//        }
        // To avoid the the issues with above approach and to make it industry-standard use ParameterizedTypeReference
        try{
            // 1. Use exchange() with ParameterizedTypeRefence to preserve the <CustomerDetails> generic type
            ResponseEntity<ResponseVO<CustomerDetails>> response = restTemplate.exchange(
                    CUSTOMER_DETAILS_URL + req.getCustomerId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseVO<CustomerDetails>>() {}
            );
            if(response.getStatusCode().is2xxSuccessful() && response.getBody()!=null){
                ResponseVO<CustomerDetails> res = response.getBody();
                if(res.getStatusCode()==HttpStatus.OK.value()){
                    // No type casting needed here anymore
                    customerDetails = res.getResult();
                }else{
                    logger.error("Error occurred while fetching customer details: {}", res.getMsg());
                    throw new ResourceNotFoundException("Error occured while fetching customer details with response as "+res.getMsg());
                }
            }else{
                logger.error("Error occured with status code : "+response.getStatusCode());
                throw new ResourceNotFoundException("Error occured while fetching customer details with status code "+response.getStatusCode());
            }
        }catch (ResourceNotFoundException e){
            logger.error("Resource not found exception occurred : ",e);
          //  throw new ResourceNotFoundException("Error occured while fetching customer details ");
            throw e;
        }catch (Exception e){
            logger.error("Exception occurred while fetching customer details: ",e);
            throw new RuntimeException("Exception occurred while fetching customer details");
        }
        if(customerDetails==null){
            logger.error("Customer details payload is missing or null for the request! ");
            throw new ResourceNotFoundException("Customer Details not found!");
        }
        CustomerAccount customerAccount = new CustomerAccount();
        customerAccount.setCustomerId(customerDetails.getCustomerId());

        if(req.getBankId()==null) throw new BadRequestException("To fetch the bank details the bank Id is required in request body");
        BankDetails bankDetails = bankRepository.findById(req.getBankId()).orElseThrow(() -> new ResourceNotFoundException("Bank details not found with id "+req.getBankId()));
        customerAccount.setBankDetails(bankDetails);

        customerAccount.setAccountType(req.getAccountType());

        if(req.getBalance().compareTo(BigDecimal.ZERO)<0) throw new BadRequestException("Invalid Balance value!");

        customerAccount.setBalance(req.getBalance());

        customerAccount = accountRepository.save(customerAccount);

        logger.info("Customer Account added successfully! with id "+customerAccount.getAccountId());
        ResponseVO res = new ResponseVO();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMsg("Customer Account added successfully! with id "+customerAccount.getAccountId());
        res.setResult(req);
        return new ResponseEntity<>(res,HttpStatus.CREATED);
    }

//    private CustomerAccount getById(Long accId){
//        if(accId==null) throw new BadRequestException("To fetch the customer account the account Id is required in request body");
//        CustomerAccount customerAccount = accountRepository.findById(accId).orElseThrow(() -> new ResourceNotFoundException("Customer Account not found with id "+accId));
//        if(customerAccount==null) throw new ResourceNotFoundException("Customer Account not found with id "+accId);
//        return customerAccount;
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ResponseVO<Boolean>> transferAmount(AmountTransferReqDto req){

        validateTransferAmountRequest(req);

        ProcessedTransaction processedTransaction = new ProcessedTransaction();
        processedTransaction.setFromAccount(req.getFromAccount());
        processedTransaction.setToAccount(req.getToAccount());
        processedTransaction.setTransCode(req.getTransCode().trim());
        processedTransaction.setAmount(req.getAmount());
        processedTransaction.setProcessedTransCreatedAt(LocalDateTime.now());
//        processedTransactionRepository.save(processedTransaction);
//
        try{
            // Force Hibernate to flush the insert statement to the DB instantly
            // This forces the DB engine to evaluate the unique constraint on "trans_code"
            processedTransactionRepository.saveAndFlush(processedTransaction);
        }catch (DataIntegrityViolationException e){
            logger.error("Idempotency match detected via database constraint for transCode: {}. Processing silent playback.", req.getTransCode());
            ResponseVO<Boolean> playbackRes = new ResponseVO<>();
            playbackRes.setMsg("Amount transfer already processed successfully! (Idempotent Playback)");
            playbackRes.setStatusCode(HttpStatus.OK.value());
            playbackRes.setResult(true);
            return new ResponseEntity<>(playbackRes,HttpStatus.OK);
        }

        // deadlock prevention via ordered locking
        Long firstId = Math.min(req.getFromAccount(), req.getToAccount());
        Long secondId = Math.max(req.getFromAccount(), req.getToAccount());

        // Pessistement locking block
        // row is lock , and other transaction for same row is wait until the current transaction is commit or roll back
        // handle the situation where two trasaction hit the same row at same time(e.g. at same second)
        Optional<CustomerAccount> firstAccount = accountRepository.findByIdForUpdate(firstId);
        if(firstAccount.isEmpty()){
            throw new ResourceNotFoundException("Customer Account not found with id "+firstId);
        }

        Optional<CustomerAccount> secondAccount = accountRepository.findByIdForUpdate(secondId);
        if(secondAccount.isEmpty()){
            throw new ResourceNotFoundException("Customer Account not found with id "+secondId);
        }

        CustomerAccount fromCustomerAccount = firstId.equals(req.getFromAccount())? firstAccount.get() : secondAccount.get();
        CustomerAccount toCustomerAccount = secondId.equals(req.getToAccount())?secondAccount.get() : firstAccount.get();

        if(fromCustomerAccount.getAccountStatus() != AccountStatus.ACTIVE){
            throw new BadRequestException("Source account is not ACTIVE. Current Status: "+fromCustomerAccount.getAccountStatus());
        }

        if(toCustomerAccount.getAccountStatus() != AccountStatus.ACTIVE){
            throw new BadRequestException("Destination account is not ACTIVE. Current Status: "+fromCustomerAccount.getAccountStatus());
        }

        BigDecimal fromAccountBalance = fromCustomerAccount.getBalance();
        if(fromAccountBalance.compareTo(req.getAmount()) < 0){
            throw new BadRequestException("Account has Insufficient balance for transfer!");
        }

        // destination account balance
        BigDecimal toAccountBalance = toCustomerAccount.getBalance();

        fromCustomerAccount.setBalance(fromAccountBalance.subtract(req.getAmount()));

        toCustomerAccount.setBalance(toCustomerAccount.getBalance().add(req.getAmount()));

        accountRepository.save(fromCustomerAccount);
        accountRepository.save(toCustomerAccount);

        // DEBIT (DR) -> Decrease a liability / decrease customer account balance
        // Money leaving fromCustomerAccount
        LedgerAudit ledgerAuditDebit = LedgerAudit
                .builder()
                .accountId(fromCustomerAccount.getAccountId())
                .transCode(req.getTransCode().trim())
                .entryType(EntryType.DEBIT)
                .amount(req.getAmount())
                .balanceBefore(fromAccountBalance)
                .balanceAfter(fromCustomerAccount.getBalance())
                .build();

        // CREDIT (CR) -> Increases a liability / increase customer account balance
        // Money entering toCustomerAccount
        LedgerAudit ledgerAuditCredit = LedgerAudit
                .builder()
                .accountId(toCustomerAccount.getAccountId())
                .transCode(req.getTransCode().trim())
                .entryType(EntryType.CREDIT)
                .amount(req.getAmount())
                .balanceBefore(toAccountBalance)
                .balanceAfter(toCustomerAccount.getBalance())
                .build();

        ledgerAuditRepository.saveAll(List.of(ledgerAuditDebit,ledgerAuditCredit));

        logger.info("From Account Id "+fromCustomerAccount.getAccountId()+" "+req.getAmount()+" amount transfer to Account Id "+toCustomerAccount.getAccountId());
        ResponseVO res = new ResponseVO();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMsg("Amount transfer successfully!");
        res.setResult(true);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

//    @Override
//    public ResponseEntity<ResponseVO<Boolean>> verifyProcessedTransaction(String transCode) {
//
//        Boolean exists = processedTransactionRepository.existsByTransCode(transCode);
//
//        logger.info("ProcessedTransaction present | transCode={} | exists={}",transCode,exists);
//
//        ResponseVO<Boolean> res = new ResponseVO<>();
//        res.setStatusCode(HttpStatus.OK.value());
//        res.setMsg(exists?
//                "Processed Transaction present in account-service."
//                : "Processed Transaction not present in account-service.");
//        res.setResult(exists);
//        return new ResponseEntity<>(res,HttpStatus.OK);
//    }

    @Override
    public ResponseEntity<ResponseVO<Set<String>>> verifyProcessedTransactions(Set<String> transCodes) {
        if(transCodes==null || transCodes.isEmpty()){
            ResponseVO<Set<String>> res = new ResponseVO<>();
            res.setStatusCode(HttpStatus.BAD_REQUEST.value());
            res.setMsg("Input data not found");
            res.setResult(null);
            return new ResponseEntity<>(res,HttpStatus.BAD_REQUEST);
        }
        Optional<Set<String>> existingTransCodesOpt = processedTransactionRepository.findByTransCodes(transCodes);
        Set<String> existingTransCodes = existingTransCodesOpt.orElse(new HashSet<>());

        ResponseVO<Set<String>> res = new ResponseVO<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMsg("Successfully fetched existing processed transactions.");
        res.setResult(existingTransCodes);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

    private void validateTransferAmountRequest(AmountTransferReqDto req){
        if (req == null) {
            throw new BadRequestException("Request body cannot be null.");
        }
        if (req.getTransCode() == null || req.getTransCode().trim().isEmpty()) {
            throw new BadRequestException("Transaction code is required for transfer processing.");
        }
        if(req.getFromAccount()==req.getToAccount()){
            throw new BadRequestException("Cannot transfer amount to same account!");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid transfer amount provided.");
        }
//        if (req.getAmount().compareTo(BigDecimal.valueOf(50000d)) > 0) {
//            throw new BadRequestException("Transfer amount limit exceed!");
//        }
        if (req.getAmount().compareTo(MAX_LIMIT_TRANSFER) > 0) {
            throw new BadRequestException("Transfer amount limit exceed!");
        }
    }

//    private ResponseEntity<ResponseVO<Transactions>> verifyTransactionExist(Transactions existingTransaction){
//        ResponseVO<Transactions> responseVO = new ResponseVO<>();
//        switch(existingTransaction.getTransStatus()){
//            case SUCCESS :
//                responseVO.setStatusCode(HttpStatus.CREATED.value());
//                responseVO.setMsg("Amount is transfer successfully! (Replayed Entry)");
//                responseVO.setResult(existingTransaction);
//                return new ResponseEntity<>(responseVO,HttpStatus.CREATED);
//
//            case FAILED:
//                responseVO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//                responseVO.setMsg("Amount is failed to transfer! (Replayed Entry)");
//                responseVO.setResult(existingTransaction);
//                return new ResponseEntity<>(responseVO,HttpStatus.INTERNAL_SERVER_ERROR);
//
//            case SUSPENDED:
//                responseVO.setStatusCode(HttpStatus.ACCEPTED.value());
//                responseVO.setMsg("Transaction status is uncertain due to a network or server error. It will be reconciled shortly. (Replayed Entry)");
//                responseVO.setResult(existingTransaction);
//                return new ResponseEntity<>(responseVO,HttpStatus.ACCEPTED);
//
//            case INITIALED:
//            default: throw new BadRequestException("A request processing loop is already active for transaction reference: " + existingTransaction.getTransCode());
//        }
//    }
}
