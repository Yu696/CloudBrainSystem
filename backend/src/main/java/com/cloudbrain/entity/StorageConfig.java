package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("storage_config")
public class StorageConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("config_id")
    private String configId;

    @TableField("storage_type")
    private Integer storageType;

    private String endpoint;

    private String bucket;

    @TableField("access_key")
    private String accessKey;

    @TableField("secret_key")
    private String secretKey;

    @TableField("max_capacity")
    private Long maxCapacity;

    @TableField("used_capacity")
    private Long usedCapacity;

    private Integer priority;

    private Integer status;
}
