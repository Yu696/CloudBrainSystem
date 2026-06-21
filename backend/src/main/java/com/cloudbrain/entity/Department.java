package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("department_id")
    private String departmentId;

    private String name;

    @TableField("parent_id")
    private String parentId;

    private String category;

    private String description;

    private String location;

    private String phone;

    @TableField("sort_order")
    private Integer sortOrder = 0;

    private Integer status = 1;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
