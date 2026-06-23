package com.cloudbrain.dto.response;

import lombok.Data;

@Data
public class UserRoleVO {
    private String roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private String departmentId;
    private String title;
    private java.math.BigDecimal consultationFee;
    private String specialty;
    private String introduction;
}
