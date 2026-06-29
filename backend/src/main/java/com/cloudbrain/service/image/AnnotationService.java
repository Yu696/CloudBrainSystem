package com.cloudbrain.service.image;

import com.cloudbrain.dto.request.image.ImageAnnotateRequest;
import com.cloudbrain.dto.response.image.AnnotationVO;

import java.util.List;

public interface AnnotationService {

    /** 创建标注 */
    AnnotationVO create(ImageAnnotateRequest request);

    /** 查询某影像的所有标注 */
    List<AnnotationVO> listByImageId(String imageId);

    /** 删除标注 */
    void delete(String annotationId);

    /** 修改标注 */
    AnnotationVO update(String annotationId, ImageAnnotateRequest request);
}
