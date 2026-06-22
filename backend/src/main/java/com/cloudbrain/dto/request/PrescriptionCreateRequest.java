package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PrescriptionCreateRequest {

    @NotBlank(message = "病历ID不能为空")
    private String recordId;

    private String prescriptionDesc;

    @NotEmpty(message = "处方明细不能为空")
    private List<PrescriptionItemRequest> items;

    /** 处方状态：0=草稿 1=待审核，默认草稿 */
    private Integer status;

    @Data
    public static class PrescriptionItemRequest {
        @NotBlank(message = "药品ID不能为空")
        private String drugId;

        @NotBlank(message = "药品名称不能为空")
        private String drugName;

        private String spec;
        private String dosage;
        private String frequency;
        private String administration;
        private Integer days;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String remark;
    }
}
