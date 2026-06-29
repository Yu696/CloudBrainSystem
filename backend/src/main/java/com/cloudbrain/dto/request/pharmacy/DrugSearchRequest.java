package com.cloudbrain.dto.request.pharmacy;

import lombok.Data;

@Data
public class DrugSearchRequest {

    private String keyword;

    private String category;

    private Integer prescriptionType;
}
