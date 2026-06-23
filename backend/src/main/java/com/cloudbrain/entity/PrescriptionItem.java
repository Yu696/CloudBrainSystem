package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("prescription_item")
public class PrescriptionItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private String itemId;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("drug_id")
    private String drugId;

    @TableField("drug_name")
    private String drugName;

    private String spec;

    private String dosage;

    private String frequency;

    private String administration;

    private Integer days;

    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private String remark;
}
