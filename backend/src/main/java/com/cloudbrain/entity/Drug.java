package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("drug_id")
    private String drugId;

    @TableField("drug_code")
    private String drugCode;

    @TableField("drug_name")
    private String drugName;

    @TableField("generic_name")
    private String genericName;

    /** 主要成分（阶段三补充） */
    private String ingredients;

    private String spec;

    @TableField("dosage_form")
    private String dosageForm;

    private String manufacturer;

    private String unit;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    /** 进价（阶段三补充） */
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    @TableField("usage_method")
    private String usageMethod;

    @TableField("cautious_crowd")
    private String cautiousCrowd;

    @TableField("side_effects")
    private String sideEffects;

    @TableField("drug_category")
    private String drugCategory;

    @TableField("prescription_type")
    private Integer prescriptionType;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
