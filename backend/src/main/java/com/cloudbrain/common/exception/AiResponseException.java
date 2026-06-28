package com.cloudbrain.common.exception;

/** AI 响应格式异常 */
public class AiResponseException extends RuntimeException {
    public AiResponseException(String message) {
        super(message);
    }
    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
