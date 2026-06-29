package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DrugAddRequest {

    private String drugCode;

    @NotBlank(message = "药品名称不能为空")
    private String drugName;

    private String genericName;

    private String ingredients;

    private String spec;

    private String dosageForm;

    private String manufacturer;

    @NotBlank(message = "单位不能为空")
    private String unit;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private BigDecimal purchasePrice;

    private String usageMethod;

    private String cautiousCrowd;

    private String sideEffects;

    private String drugCategory;

    @NotNull(message = "处方类型不能为空")
    private Integer prescriptionType;
}
