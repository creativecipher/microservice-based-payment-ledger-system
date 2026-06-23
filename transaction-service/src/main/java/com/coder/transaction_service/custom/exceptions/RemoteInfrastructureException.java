package com.coder.transaction_service.custom.exceptions;

public class RemoteInfrastructureException extends RuntimeException{
    private final String tranCode;

    public RemoteInfrastructureException(String message,String tranCode){
        super(message);
        this.tranCode=tranCode;
    }
    public RemoteInfrastructureException(String message,String tranCode,Throwable cause){
        super(message,cause);
        this.tranCode=tranCode;
    }

    public String getTransCode() {
        return this.tranCode;
    }
}
