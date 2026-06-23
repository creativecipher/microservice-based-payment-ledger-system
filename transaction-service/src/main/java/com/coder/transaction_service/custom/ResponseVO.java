package com.coder.transaction_service.custom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseVO<T> {
    private int statusCode;
    private String msg;
    private T result;
}

