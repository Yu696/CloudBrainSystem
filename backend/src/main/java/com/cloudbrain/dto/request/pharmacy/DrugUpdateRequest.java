package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DrugUpdateRequest {

    @NotBlank(message = "药品ID不能为空")
    private String drugId;

    private String drugCode;

    private String drugName;

    private String genericName;

    private String ingredients;

    private String spec;

    private String dosageForm;

    private String manufacturer;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal purchasePrice;

    private String usageMethod;

    private String cautiousCrowd;

    private String sideEffects;

    private String drugCategory;

    private Integer prescriptionType;
}
