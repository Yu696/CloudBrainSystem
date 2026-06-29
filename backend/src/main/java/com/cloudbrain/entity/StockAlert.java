package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stock_alert")
public class StockAlert {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("drug_id")
    private String drugId;

    @TableField("alert_type")
    private Integer alertType;

    @TableField("current_stock")
    private Integer currentStock;

    private Integer threshold;

    @TableField("alert_message")
    private String alertMessage;

    @TableField("is_handled")
    private Integer isHandled;

    @TableField("handled_by")
    private String handledBy;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
