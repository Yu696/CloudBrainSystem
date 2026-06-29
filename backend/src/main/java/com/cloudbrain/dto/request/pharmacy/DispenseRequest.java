package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class DispenseRequest {

    @NotBlank(message = "处方ID不能为空")
    private String prescriptionId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotEmpty(message = "发药明细不能为空")
    @Valid
    private List<DispenseItem> items;

    @Data
    public static class DispenseItem {
        @NotBlank(message = "药品ID不能为空")
        private String drugId;

        @jakarta.validation.constraints.Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
