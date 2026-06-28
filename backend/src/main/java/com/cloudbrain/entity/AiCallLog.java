package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 调用日志（统一日志表）
 * 覆盖分诊、诊断、处方审核三种调用类型
 */
@Data
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("call_id")
    private String callId;

    @TableField("call_type")
    private Integer callType;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("input_summary")
    private String inputSummary;

    @TableField("input_data")
    private String inputData;

    @TableField("output_data")
    private String outputData;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("ai_model")
    private String aiModel;

    @TableField("response_time_ms")
    private Integer responseTimeMs;

    private Integer status;

    @TableField("error_message")
    private String errorMessage;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
