package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("examination_order")
public class ExaminationOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("record_id")
    private String recordId;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("exam_category")
    private Integer examCategory;

    @TableField("exam_name")
    private String examName;

    @TableField("exam_purpose")
    private String examPurpose;

    private BigDecimal amount = BigDecimal.ZERO;

    private Integer status = 0;

    @TableField("is_ai_recommended")
    private Integer isAiRecommended = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
