package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("permission_id")
    private String permissionId;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("parent_id")
    private String parentId = "0";

    private Integer type;

    private String path;

    @TableField("sort_order")
    private Integer sortOrder = 0;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
