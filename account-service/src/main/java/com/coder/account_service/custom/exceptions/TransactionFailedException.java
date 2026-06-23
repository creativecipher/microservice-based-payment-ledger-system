package com.coder.account_service.custom.exceptions;

public class TransactionFailedException extends RuntimeException{
    public TransactionFailedException(String msg){
        super(msg);
    }
}
