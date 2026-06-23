package com.coder.transaction_service.custom.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandling {

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest req){
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                // Security tip: Avoid exposing raw system error messages to clients
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected bad request occurred.")
                .path(req.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<ErrorResponse> handleTransactionFailedException(TransactionFailedException ex, WebRequest request){
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value()) // 422
                .error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected transaction exception error occurred." )
                .path(request.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(RemoteInfrastructureException.class)
    public ResponseEntity<ErrorResponse> handleRemoteInfrastructureException(RemoteInfrastructureException ex,WebRequest request){
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.ACCEPTED.value()) // 422
                .error("Transaction State Uncertain")
                .message("Downstream execution timed out. The transaction is pinned for auto-reconciliation. Reference Code: " + ex.getTransCode() )
                .path(request.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.ACCEPTED);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex, WebRequest request){
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                // Security tip: Avoid exposing raw system error messages to clients
                .message("An unexpected internal error occurred. Please verify transaction state via status inquiry API before retrying.")
                .path(request.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
