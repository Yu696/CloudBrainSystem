package com.cloudbrain.service.image;

import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.image.ConvertResultVO;
import com.cloudbrain.dto.response.image.ImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    /** 上传影像 */
    ImageVO upload(MultipartFile file, String patientId, String examinationId, String modality, String bodyPart);

    /** 批量上传 */
    java.util.List<ImageVO> batchUpload(MultipartFile[] files, String patientId, String examinationId, String modality, String bodyPart);

    /** 影像详情 */
    ImageVO getInfo(String imageId);

    /** 影像列表分页查询 */
    PageResult<ImageVO> list(String patientId, String examinationId, String modality, Boolean myExams, int page, int pageSize);

    /** 影像预览（返回字节流） */
    byte[] preview(String imageId);

    /** 删除影像 */
    void delete(String imageId);

    /** 影像对比（返回两张影像的预览 URL） */
    java.util.Map<String, String> compare(String imageId1, String imageId2);

    /** 格式转换 */
    ConvertResultVO convert(String imageId, String targetFormat);
}
