package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExaminationResultCreateRequest {

    /** 检查单ID（不传则通过 imageId 查找或自动创建检查单） */
    private String orderId;

    /** 影像ID（orderId 为空时用于查找或自动创建检查单） */
    private String imageId;

    /** 检查结果数据 */
    private String resultData;

    /** 参考范围 */
    private String referenceRange;

    /** 是否异常 0=正常 1=异常 */
    private Integer isAbnormal;

    /** 医生意见 */
    private String doctorOpinion;

    /** 报告文件URL */
    private String reportFileUrl;

    /** AI 分析结果 */
    private String aiAnalysis;
}
