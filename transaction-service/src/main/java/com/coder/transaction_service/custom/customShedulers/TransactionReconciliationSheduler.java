package com.coder.transaction_service.custom.customShedulers;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.custom.exceptions.BadRequestException;
import com.coder.transaction_service.custom.exceptions.RemoteInfrastructureException;
import com.coder.transaction_service.custom.exceptions.TransactionFailedException;
import com.coder.transaction_service.custom.gateways.AccountServiceGateway;
import com.coder.transaction_service.dataLayer.TransactionsRepository;
import com.coder.transaction_service.modelLayer.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Log4j2
public class TransactionReconciliationSheduler {

    private final TransactionsRepository transactionsRepository;
    private final AccountServiceGateway accountServiceGateway;

    //"30 * * * * *" -> means every single minute, exactly at the 30th second mark (e.g., 12:01:30, 12:02:30, 12:03:30).
    @Scheduled(cron = "*/30 * * * * *",zone="Asia/Kolkata")
  //  @Transactional -> No @Transactional here! Keeps DB connection pool responsive during remote HTTP IO calls
    public void reconcileRecentTransaction(){
        List<TransactionStatus> transactionStatusList = List.of(TransactionStatus.INITIALED,TransactionStatus.SUSPENDED);

        // Enforce safe 30-second aging threshold to give original processing threads breathing room
        LocalDateTime agingThreshold = LocalDateTime.now().minusSeconds(30);

        Optional<Set<String>> strandedCodesOpt = transactionsRepository.findByTransStatusAndTransCreatedAt(transactionStatusList,agingThreshold);

        if(strandedCodesOpt.isEmpty() || strandedCodesOpt.get().isEmpty()){
            log.debug("No stranded INITIALIZED or SUSPENDED transactions found for auto-reconciliation.");
            return;
        }

        //Wrapped in a new HashSet to guarantee full modifiability and prevent UnsupportedOperationException
        Set<String> unClearifyStatusTransaction = new HashSet<>(strandedCodesOpt.get());
        log.info("Found {} stranded transaction(s) requiring verification lookup.", unClearifyStatusTransaction.size());

        try{
            ResponseEntity<ResponseVO<Set<String>>> response = accountServiceGateway.verifyProcessedTransaction(unClearifyStatusTransaction);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody()!=null){
                ResponseVO<Set<String>> res = response.getBody();

                if(res.getStatusCode()== HttpStatus.OK.value() ){
                    Set<String> confirmedSuccessfulTransCodes = res.getResult();

                    // Settle confirmed matches to SUCCESS via short, isolated write
                    if(!confirmedSuccessfulTransCodes.isEmpty()){

                    }
                    unClearifyStatusTransaction.removeAll(confirmedSuccessfulTransCodes);

                    if(!unClearifyStatusTransaction.isEmpty()){
                        int updatedTransactionCount = transactionsRepository.updateTransactionStatus(unClearifyStatusTransaction,TransactionStatus.FAILED);
                        log.info("{} recent transaction(s) were not found in Account-Service and marked as FAILED",updatedTransactionCount);
                    }else{
                        log.info("All recent pending transactions match perfectly with Account-Service processing history.");
                    }
                    return;
                }else{
                    log.info("Failed to verify transfer transaction from Account Service due to :{} with status-code: {}",res.getMsg(),res.getStatusCode());
                }
            }
                log.warn("Account service returned unmanaged non-2xx status code {} for verify transfer transactions.", response.getStatusCode());
            }catch(HttpClientErrorException e){
                log.error("Client side exception (HTTP 4xx) from Account Service for verify transfer recent transaction" , e);
            } catch (Exception e) {
                // Catch-all for unexpected local exceptions (like JSON parsing glitches)
                log.error("Unexpected exception occurred while verifying transfers", e);
            }
  //      }
//        else{
//            log.debug("No INITIALIZED or SUSPENDED transactions found for reconciliation.");
//        }
    }
}


