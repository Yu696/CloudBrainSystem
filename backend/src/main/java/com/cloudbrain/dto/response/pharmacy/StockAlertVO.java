package com.cloudbrain.dto.response.pharmacy;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class StockAlertVO {

    private Long id;
    private String drugId;
    private String drugName;
    private String warehouseId;
    private String warehouseName;
    private Integer alertType;
    private String alertTypeName;
    private Integer currentStock;
    private Integer threshold;
    private String alertMessage;
    private Boolean isHandled;
    private String handledBy;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;

    public static String alertTypeName(Integer alertType) {
        return switch (alertType != null ? alertType : -1) {
            case 0 -> "低库存";
            case 1 -> "过期预警";
            case 2 -> "库存积压";
            default -> "未知";
        };
    }
}
