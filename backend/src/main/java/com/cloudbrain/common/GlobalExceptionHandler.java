package com.cloudbrain.common;

import com.cloudbrain.common.exception.AiResponseException;
import com.cloudbrain.common.exception.AiTimeoutException;
import com.cloudbrain.common.exception.AiUnavailableException;
import com.cloudbrain.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 数据库唯一约束冲突 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg = e.getMessage();
        log.warn("数据唯一约束冲突: {}", msg);

        // 解析 MySQL 唯一约束字段
        if (msg != null) {
            if (msg.contains("uk_phone") || msg.contains("'phone'") || msg.contains("idx_phone")) {
                return Result.fail("该手机号已被其他用户绑定，请更换手机号");
            }
            if (msg.contains("uk_id_card") || msg.contains("'id_card'") || msg.contains("idx_id_card")) {
                return Result.fail("该身份证号已存在，请检查后重试");
            }
            if (msg.contains("uk_medical_record") || msg.contains("medical_record_no")) {
                return Result.fail("该病历号已存在");
            }
            if (msg.contains("Duplicate entry")) {
                // 尝试从错误信息中提取字段名
                String fieldName = extractDuplicateField(msg);
                return Result.fail("[" + fieldName + "] 已存在，请勿重复提交");
            }
        }
        return Result.fail("数据重复，请检查后重试");
    }

    /** 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.fail(msg);
    }

    /** AI 调用超时 */
    @ExceptionHandler(AiTimeoutException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleAiTimeout(AiTimeoutException e) {
        log.error("AI 调用超时: {}", e.getMessage());
        return Result.serverError("AI 服务响应超时，已为您提供备用方案");
    }

    /** AI 服务不可达 */
    @ExceptionHandler(AiUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleAiUnavailable(AiUnavailableException e) {
        log.error("AI 服务不可用: {}", e.getMessage());
        return Result.serverError("AI 服务暂时不可用，已为您提供备用方案");
    }

    /** AI 响应格式异常 */
    @ExceptionHandler(AiResponseException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleAiResponse(AiResponseException e) {
        log.error("AI 响应异常: {}", e.getMessage());
        return Result.serverError("AI 服务响应异常，已为您提供备用方案");
    }

    /** 运行时异常 */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("系统异常", e);
        return Result.serverError("服务器内部错误");
    }

    /** 其他异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("未知异常", e);
        return Result.serverError("服务器内部错误");
    }

    /** 从 MySQL 唯一约束错误信息中提取字段名 */
    private String extractDuplicateField(String errorMsg) {
        // 格式: Duplicate entry 'xxx' for key 'uk_phone'
        int keyIdx = errorMsg.indexOf("for key '");
        if (keyIdx > 0) {
            String keyPart = errorMsg.substring(keyIdx + 9);
            int endIdx = keyPart.indexOf("'");
            if (endIdx > 0) {
                String key = keyPart.substring(0, endIdx);
                // 将常见的索引名映射为中文
                return switch (key) {
                    case "uk_phone", "idx_phone" -> "手机号";
                    case "uk_id_card", "idx_id_card" -> "身份证号";
                    case "uk_user_id", "idx_user_id" -> "关联用户";
                    case "uk_medical_record_no" -> "病历号";
                    default -> key;
                };
            }
        }
        return "该记录";
    }
}
