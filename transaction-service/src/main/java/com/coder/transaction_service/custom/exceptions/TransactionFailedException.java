package com.coder.transaction_service.custom.exceptions;

public class TransactionFailedException extends RuntimeException{
    public TransactionFailedException(String msg){
        super(msg);
    }
}
