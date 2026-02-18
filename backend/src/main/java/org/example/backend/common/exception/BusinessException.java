package org.example.backend.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private Integer code;       // 错误码
    private String msg;         // 错误消息

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
