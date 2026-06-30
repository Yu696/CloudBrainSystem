package com.cloudbrain.dto.response;

import com.cloudbrain.entity.Drug;
import com.cloudbrain.entity.PrescriptionItem;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PrescriptionItemVO {

    private String itemId;
    private String prescriptionId;
    private String drugId;
    private String drugName;
    private String drugCode;
    private String genericName;
    private String spec;
    private String dosageForm;
    private String manufacturer;
    private String unit;
    private String drugCategory;
    private Integer prescriptionType;
    private String usageMethod;
    private String cautiousCrowd;
    private String sideEffects;
    private String dosage;
    private String frequency;
    private String administration;
    private Integer days;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String remark;

    public static PrescriptionItemVO from(PrescriptionItem item) {
        return PrescriptionItemVO.builder()
                .itemId(item.getItemId())
                .prescriptionId(item.getPrescriptionId())
                .drugId(item.getDrugId())
                .drugName(item.getDrugName())
                .spec(item.getSpec())
                .dosage(item.getDosage())
                .frequency(item.getFrequency())
                .administration(item.getAdministration())
                .days(item.getDays())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .remark(item.getRemark())
                .build();
    }

    public static PrescriptionItemVO from(PrescriptionItem item, Drug drug) {
        PrescriptionItemVO vo = from(item);
        if (drug != null) {
            vo.setDrugCode(drug.getDrugCode());
            vo.setGenericName(drug.getGenericName());
            vo.setDosageForm(drug.getDosageForm());
            vo.setManufacturer(drug.getManufacturer());
            vo.setUnit(drug.getUnit());
            vo.setDrugCategory(drug.getDrugCategory());
            vo.setPrescriptionType(drug.getPrescriptionType());
            vo.setUsageMethod(drug.getUsageMethod());
            vo.setCautiousCrowd(drug.getCautiousCrowd());
            vo.setSideEffects(drug.getSideEffects());
        }
        return vo;
    }
}
