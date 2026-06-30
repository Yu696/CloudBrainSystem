package com.cloudbrain.controller.image;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.image.ImageAnnotateRequest;
import com.cloudbrain.dto.request.image.ImageCompareRequest;
import com.cloudbrain.dto.request.image.ImageConvertRequest;
import com.cloudbrain.dto.response.image.AnnotationVO;
import com.cloudbrain.dto.response.image.ConvertResultVO;
import com.cloudbrain.dto.response.image.ImageVO;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.service.image.AnnotationService;
import com.cloudbrain.service.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 影像管理 Controller — M05 影像管理
 * 9 个影像管理 API
 */
@Tag(name = "影像管理")
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'RADIOLOGIST')")
public class ImageController extends BaseController {

    private final ImageService imageService;
    private final AnnotationService annotationService;

    // ==================== IM-01 影像上传 ====================

    @Operation(summary = "上传影像（IM-01）")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ImageVO> upload(
            @Parameter(description = "影像文件（最大 50MB）") @RequestPart("file") MultipartFile file,
            @Parameter(description = "患者 ID") @RequestParam String patientId,
            @Parameter(description = "关联检查单 ID") @RequestParam(required = false) String examinationId,
            @Parameter(description = "成像设备（CT/MRI/X光/超声）") @RequestParam(required = false) String modality,
            @Parameter(description = "身体部位") @RequestParam(required = false) String bodyPart) {
        return success(imageService.upload(file, patientId, examinationId, modality, bodyPart));
    }

    // ==================== IM-02 批量上传 ====================

    @Operation(summary = "批量上传影像（IM-02）")
    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<ImageVO>> batchUpload(
            @Parameter(description = "影像文件列表") @RequestPart("files") MultipartFile[] files,
            @Parameter(description = "患者 ID") @RequestParam String patientId,
            @Parameter(description = "关联检查单 ID") @RequestParam(required = false) String examinationId,
            @Parameter(description = "成像设备") @RequestParam(required = false) String modality,
            @Parameter(description = "身体部位") @RequestParam(required = false) String bodyPart) {
        return success(imageService.batchUpload(files, patientId, examinationId, modality, bodyPart));
    }

    // ==================== IM-03 影像信息查询 ====================

    @Operation(summary = "查询影像信息（IM-03）")
    @GetMapping("/info")
    public Result<ImageVO> info(@Parameter(description = "影像 ID") @RequestParam String imageId) {
        return success(imageService.getInfo(imageId));
    }

    @Operation(summary = "影像列表分页查询（IM-03）" +
            " - 医生默认返回自己开单的影像，管理员可查看全部")
    @GetMapping("/list")
    public Result<PageResult<ImageVO>> list(
            @Parameter(description = "按患者 ID 筛选") @RequestParam(required = false) String patientId,
            @Parameter(description = "按患者姓名筛选") @RequestParam(required = false) String patientName,
            @Parameter(description = "按检查单筛选") @RequestParam(required = false) String examinationId,
            @Parameter(description = "按设备筛选") @RequestParam(required = false) String modality,
            @Parameter(description = "仅看自己开单的影像（医生默认 true）") @RequestParam(required = false) Boolean myExams,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return success(imageService.list(patientId, patientName, examinationId, modality, myExams, page, pageSize));
    }

    // ==================== IM-04 影像预览 ====================

    @Operation(summary = "影像预览（IM-04）")
    @GetMapping("/preview")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> preview(@Parameter(description = "影像 ID") @RequestParam String imageId) {
        String accessUrl = imageService.getPreviewUrl(imageId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", accessUrl)
                .build();
    }

    // ==================== IM-05 影像删除 ====================

    @Operation(summary = "删除影像（IM-05）")
    @DeleteMapping("/delete")
    public Result<String> delete(@Parameter(description = "影像 ID") @RequestParam String imageId) {
        imageService.delete(imageId);
        return success("删除成功");
    }

    // ==================== IM-06 影像标注 ====================

    @Operation(summary = "创建影像标注（IM-06）")
    @PostMapping("/annotate")
    public Result<AnnotationVO> annotate(@Valid @RequestBody ImageAnnotateRequest request) {
        return success(annotationService.create(request));
    }

    @Operation(summary = "查询影像标注列表")
    @GetMapping("/annotate/list")
    public Result<List<AnnotationVO>> annotateList(@Parameter(description = "影像 ID") @RequestParam String imageId) {
        return success(annotationService.listByImageId(imageId));
    }

    @Operation(summary = "更新影像标注")
    @PutMapping("/annotate/{annotationId}")
    public Result<AnnotationVO> annotateUpdate(
            @Parameter(description = "标注 ID") @PathVariable String annotationId,
            @Valid @RequestBody ImageAnnotateRequest request) {
        return success(annotationService.update(annotationId, request));
    }

    @Operation(summary = "删除影像标注")
    @DeleteMapping("/annotate/delete")
    public Result<String> annotateDelete(@Parameter(description = "标注 ID") @RequestParam String annotationId) {
        annotationService.delete(annotationId);
        return success("删除成功");
    }

    // ==================== IM-08 影像对比 ====================

    @Operation(summary = "影像对比（IM-08）")
    @PostMapping("/compare")
    public Result<Map<String, String>> compare(@Valid @RequestBody ImageCompareRequest request) {
        return success(imageService.compare(request.getImageId1(), request.getImageId2()));
    }

    // ==================== IM-09 格式转换 ====================

    @Operation(summary = "格式转换（IM-09）")
    @PostMapping("/convert")
    public Result<ConvertResultVO> convert(@Valid @RequestBody ImageConvertRequest request) {
        return success(imageService.convert(request.getImageId(), request.getTargetFormat()));
    }
}
