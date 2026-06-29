package com.cloudbrain.dto.response.pharmacy;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WarehouseVO {

    private String warehouseId;
    private String name;
    private String location;
    private String adminId;
    private Integer type;
    private String typeName;
    private Integer status;
    private LocalDateTime createTime;

    public static String typeName(Integer type) {
        return switch (type != null ? type : -1) {
            case 0 -> "药库";
            case 1 -> "药房";
            default -> "未知";
        };
    }
}
