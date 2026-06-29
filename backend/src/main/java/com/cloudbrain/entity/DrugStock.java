package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("drug_stock")
public class DrugStock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("drug_id")
    private String drugId;

    @TableField("warehouse_id")
    private String warehouseId;

    @TableField("current_stock")
    private Integer currentStock;

    @TableField("min_stock")
    private Integer minStock;

    @TableField("max_stock")
    private Integer maxStock;

    @TableField("batch_no")
    private String batchNo;

    @TableField("production_date")
    private LocalDate productionDate;

    @TableField("expiry_date")
    private LocalDate expiryDate;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
