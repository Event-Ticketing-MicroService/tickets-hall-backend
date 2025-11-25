package com.ticketshall.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage);
    }

}
