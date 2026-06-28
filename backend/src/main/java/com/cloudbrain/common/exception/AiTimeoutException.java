package com.cloudbrain.common.exception;

/** AI 调用超时异常 */
public class AiTimeoutException extends RuntimeException {
    public AiTimeoutException(String message) {
        super(message);
    }
    public AiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
