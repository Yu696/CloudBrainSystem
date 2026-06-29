package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warehouse")
public class Warehouse {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("warehouse_id")
    private String warehouseId;

    private String name;

    private String location;

    @TableField("admin_id")
    private String adminId;

    private Integer type;

    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
