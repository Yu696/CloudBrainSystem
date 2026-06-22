package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("examination_result")
public class ExaminationResult {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("result_id")
    private String resultId;

    @TableField("order_id")
    private String orderId;

    @TableField("result_data")
    private String resultData;

    @TableField("reference_range")
    private String referenceRange;

    @TableField("is_abnormal")
    private Integer isAbnormal = 0;

    @TableField("ai_analysis")
    private String aiAnalysis;

    @TableField("doctor_opinion")
    private String doctorOpinion;

    @TableField("report_file_url")
    private String reportFileUrl;

    @TableField("result_time")
    private LocalDateTime resultTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
