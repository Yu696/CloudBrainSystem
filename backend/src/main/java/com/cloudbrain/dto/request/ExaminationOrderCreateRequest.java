package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExaminationOrderCreateRequest {

    @NotBlank(message = "病历ID不能为空")
    private String recordId;

    @NotBlank(message = "检查项目名称不能为空")
    private String examName;

    @NotNull(message = "检查类别不能为空")
    private Integer examCategory;

    private String examPurpose;

    /** 检查费用，不传则按项目自动计算 */
    private BigDecimal amount;
}
