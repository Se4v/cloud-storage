package org.example.backend.common.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String msg;

    public BusinessException(String msg) {
        this(500, msg);
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String msg, Throwable cause) {
        this(500, msg, cause);
    }

    public BusinessException(Integer code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return String.format("BusinessException{code=%d, msg='%s'} %s",
                code, msg, super.toString());
    }
}