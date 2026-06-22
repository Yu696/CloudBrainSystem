package com.cloudbrain.dto.response;

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
    private String spec;
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
}
