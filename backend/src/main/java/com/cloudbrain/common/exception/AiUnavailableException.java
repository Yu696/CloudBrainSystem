package com.cloudbrain.common.exception;

/** AI 服务不可达异常 */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
