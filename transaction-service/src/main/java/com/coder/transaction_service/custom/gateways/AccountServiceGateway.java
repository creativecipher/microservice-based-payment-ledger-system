package com.coder.transaction_service.custom.gateways;

import com.coder.transaction_service.custom.ResponseVO;
import com.coder.transaction_service.custom.exceptions.RemoteInfrastructureException;
import com.coder.transaction_service.dto.reDto.AmountTransferReqDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Log4j2
public class AccountServiceGateway {

    private final RestTemplate restTemplate;

    @Value("${account.service.url}")
    private String ACCOUNT_SERVICE_URL;

    @CircuitBreaker(name="transferAmountBreaker",
            fallbackMethod = "transferAmountFallbackMtd")
    public ResponseEntity<ResponseVO<Boolean>> executeRemoteTransfer(AmountTransferReqDto dto){
        HttpEntity<AmountTransferReqDto> reqDtoHttpEntity = new HttpEntity<>(dto);

        return restTemplate.exchange(
                ACCOUNT_SERVICE_URL+"transfer-amount",
                HttpMethod.POST,
                reqDtoHttpEntity,
                new ParameterizedTypeReference<ResponseVO<Boolean>>() {}
        );
    }

    /**
     * 2. Target Fallback Method
     * THis handles infrastructure issues gracefully without capturing business rules exceptions
     * **/
    // fallbackMethod execution: when any service is down
    // then fallbackMethod is get executed
    // fallbackMethod must have same return type and parameter as its calling api method
    public ResponseEntity<ResponseVO<Boolean>> transferAmountFallbackMtd(AmountTransferReqDto req, Exception e){
        log.error("Fallback is executed because account-service down so transaction cannot perform!");
        log.error("CIRCUIT BREAKER TRIGGERED for outbound call. transCode: {}. Reason: {}",req.getTransCode(),e.getMessage());
        // Throwing this guarantees our service implementation knows exactly which ledger row is stranded
        throw new RemoteInfrastructureException(
                "Account service unavailable or timed out.",
                req.getTransCode(),
                e
        );
    }
}
