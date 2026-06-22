package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentCreateRequest {
    @NotBlank(message = "科室名称不能为空")
    private String name;
    private String parentId;
    private String category;
    private String description;
    private String location;
    private String phone;
    private Integer sortOrder;
}
