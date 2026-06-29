package com.cloudbrain.dto.response.pharmacy;

import com.cloudbrain.entity.Drug;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DrugVO {

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
    private String prescriptionTypeName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static DrugVO fromEntity(Drug drug) {
        return DrugVO.builder()
                .drugId(drug.getDrugId())
                .drugCode(drug.getDrugCode())
                .drugName(drug.getDrugName())
                .genericName(drug.getGenericName())
                .ingredients(drug.getIngredients())
                .spec(drug.getSpec())
                .dosageForm(drug.getDosageForm())
                .manufacturer(drug.getManufacturer())
                .unit(drug.getUnit())
                .unitPrice(drug.getUnitPrice())
                .purchasePrice(drug.getPurchasePrice())
                .usageMethod(drug.getUsageMethod())
                .cautiousCrowd(drug.getCautiousCrowd())
                .sideEffects(drug.getSideEffects())
                .drugCategory(drug.getDrugCategory())
                .prescriptionType(drug.getPrescriptionType())
                .prescriptionTypeName(drug.getPrescriptionType() != null && drug.getPrescriptionType() == 0 ? "OTC" : "处方药")
                .status(drug.getStatus())
                .createTime(drug.getCreateTime())
                .updateTime(drug.getUpdateTime())
                .build();
    }
}
