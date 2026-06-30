package com.cloudbrain.service.image.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.image.ConvertResultVO;
import com.cloudbrain.dto.response.image.ImageVO;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.*;
import com.cloudbrain.service.image.ImageService;
import com.cloudbrain.service.image.StorageService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final MedicalImageMapper medicalImageMapper;
    private final DoctorMapper doctorMapper;
    private final ExaminationOrderMapper examinationOrderMapper;
    private final ConversionLogMapper conversionLogMapper;
    private final PatientMapper patientMapper;
    private final StorageService storageService;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("dcm", "dicom", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Override
    @Transactional
    public ImageVO upload(MultipartFile file, String patientId, String examinationId, String modality, String bodyPart) {
        validateFile(file);

        String imageId = UUIDUtil.generateImageId();
        String filePath = storageService.store(file, imageId);
        int imageType = detectImageType(file);

        MedicalImage image = new MedicalImage();
        image.setImageId(imageId);
        image.setPatientId(patientId);
        image.setExaminationId(examinationId);
        image.setImageName(file.getOriginalFilename());
        image.setImageType(imageType);
        image.setFilePath(filePath);
        image.setFileSize(file.getSize());
        image.setModality(modality);
        image.setBodyPart(bodyPart);
        image.setUploaderId(getCurrentUserId());
        image.setUploadTime(LocalDateTime.now());
        image.setStatus(1);
        medicalImageMapper.insert(image);

        // 状态流转：已缴费(1) → 检查中(2)；仅已缴费或检查中的检查单可上传影像
        if (examinationId != null && !examinationId.isBlank()) {
            ExaminationOrder order = examinationOrderMapper.selectOne(
                    new LambdaQueryWrapper<ExaminationOrder>()
                            .eq(ExaminationOrder::getOrderId, examinationId));
            if (order != null) {
                if (order.getStatus() == 1) {
                    order.setStatus(2);
                    examinationOrderMapper.updateById(order);
                } else if (order.getStatus() != 2) {
                    throw new BusinessException("该检查单状态不允许上传影像，仅已缴费的检查单可上传");
                }
            }
        }

        return ImageVO.fromEntity(image);
    }

    @Override
    @Transactional
    public List<ImageVO> batchUpload(MultipartFile[] files, String patientId, String examinationId, String modality, String bodyPart) {
        List<ImageVO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(upload(file, patientId, examinationId, modality, bodyPart));
        }
        return results;
    }

    @Override
    public ImageVO getInfo(String imageId) {
        MedicalImage image = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId));
        if (image == null) {
            throw new BusinessException("影像不存在");
        }
        return ImageVO.fromEntity(image);
    }

    @Override
    public PageResult<ImageVO> list(String patientId, String patientName, String examinationId, String modality, Boolean myExams, int page, int pageSize) {
        LambdaQueryWrapper<MedicalImage> wrapper = new LambdaQueryWrapper<>();

        // 医生角色默认只看自己开单的影像
        if (myExams != null && myExams) {
            List<String> examOrderIds = getMyExaminationOrderIds();
            if (examOrderIds.isEmpty()) {
                return PageResult.of(0, page, pageSize, List.of());
            }
            wrapper.in(MedicalImage::getExaminationId, examOrderIds);
        }

        if (examinationId != null && !examinationId.isBlank()) {
            wrapper.eq(MedicalImage::getExaminationId, examinationId);
        }
        if (patientId != null && !patientId.isBlank()) {
            wrapper.eq(MedicalImage::getPatientId, patientId);
        }
        if (patientName != null && !patientName.isBlank()) {
            // 通过姓名查找患者 ID
            List<Patient> patients = patientMapper.selectList(
                    new LambdaQueryWrapper<Patient>().like(Patient::getName, patientName));
            List<String> pids = patients.stream().map(Patient::getPatientId).toList();
            if (pids.isEmpty()) {
                return PageResult.of(0, page, pageSize, List.of());
            }
            wrapper.in(MedicalImage::getPatientId, pids);
        }
        if (modality != null && !modality.isBlank()) {
            wrapper.eq(MedicalImage::getModality, modality);
        }
        wrapper.orderByDesc(MedicalImage::getUploadTime);

        Page<MedicalImage> result = medicalImageMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<ImageVO> records = result.getRecords().stream().map(ImageVO::fromEntity).toList();

        // 填充 patientName
        populatePatientNames(records);

        return PageResult.of(result.getTotal(), page, pageSize, records);
    }

    private void populatePatientNames(List<ImageVO> records) {
        if (records.isEmpty()) return;
        Set<String> pids = records.stream().map(ImageVO::getPatientId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        if (pids.isEmpty()) return;
        List<Patient> patients = patientMapper.selectList(
                new LambdaQueryWrapper<Patient>().in(Patient::getPatientId, pids));
        Map<String, String> nameMap = patients.stream()
                .collect(java.util.stream.Collectors.toMap(Patient::getPatientId, Patient::getName, (a, b) -> a));
        for (ImageVO vo : records) {
            if (vo.getPatientId() != null) {
                vo.setPatientName(nameMap.get(vo.getPatientId()));
            }
        }
    }

    @Override
    public byte[] preview(String imageId) {
        MedicalImage image = getImageOrThrow(imageId);
        return storageService.retrieve(image.getFilePath());
    }

    @Override
    public java.io.InputStream previewAsStream(String imageId) {
        MedicalImage image = getImageOrThrow(imageId);
        return storageService.retrieveAsStream(image.getFilePath());
    }

    @Override
    public String getPreviewUrl(String imageId) {
        MedicalImage image = getImageOrThrow(imageId);
        return storageService.getAccessUrl(image.getFilePath());
    }

    private MedicalImage getImageOrThrow(String imageId) {
        MedicalImage image = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId));
        if (image == null) {
            throw new BusinessException("影像不存在");
        }
        return image;
    }

    @Override
    @Transactional
    public void delete(String imageId) {
        MedicalImage image = getImageOrThrow(imageId);
        image.setStatus(0);
        medicalImageMapper.updateById(image);
    }

    @Override
    public Map<String, String> compare(String imageId1, String imageId2) {
        MedicalImage img1 = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId1));
        MedicalImage img2 = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId2));
        if (img1 == null || img2 == null) {
            throw new BusinessException("影像不存在");
        }
        Map<String, String> result = new HashMap<>();
        result.put("imageId1", imageId1);
        result.put("previewUrl1", "/api/image/preview?imageId=" + imageId1);
        result.put("imageId2", imageId2);
        result.put("previewUrl2", "/api/image/preview?imageId=" + imageId2);
        return result;
    }

    @Override
    @Transactional
    public ConvertResultVO convert(String imageId, String targetFormat) {
        MedicalImage image = medicalImageMapper.selectOne(
                new LambdaQueryWrapper<MedicalImage>().eq(MedicalImage::getImageId, imageId));
        if (image == null) {
            throw new BusinessException("影像不存在");
        }

        String sourceFormat = mapImageTypeToFormat(image.getImageType());
        String outputPath = image.getFilePath().replaceAll("\\.[^.]+$", "") + "." + targetFormat.toLowerCase();

        ConversionLog log = new ConversionLog();
        log.setImageId(imageId);
        log.setSourceFormat(sourceFormat);
        log.setTargetFormat(targetFormat.toUpperCase());
        log.setOutputPath(outputPath);
        log.setStatus(1);
        conversionLogMapper.insert(log);

        return ConvertResultVO.builder()
                .imageId(imageId)
                .sourceFormat(sourceFormat)
                .targetFormat(targetFormat.toUpperCase())
                .outputPath(outputPath)
                .outputSize(null)
                .status(1)
                .createTime(log.getCreateTime())
                .build();
    }

    /** 获取当前医生开过的所有检查单 ID */
    private List<String> getMyExaminationOrderIds() {
        String userId = getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            return List.of();
        }
        List<ExaminationOrder> orders = examinationOrderMapper.selectList(
                new LambdaQueryWrapper<ExaminationOrder>()
                        .eq(ExaminationOrder::getDoctorId, doctor.getDoctorId())
                        .select(ExaminationOrder::getOrderId));
        return orders.stream().map(ExaminationOrder::getOrderId).toList();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 50MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException("不支持的影像格式: " + ext);
            }
        }
    }

    private int detectImageType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null) {
            String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
            return switch (ext) {
                case "dcm", "dicom" -> 0;
                case "jpg", "jpeg" -> 1;
                case "png" -> 2;
                default -> 3;
            };
        }
        return 3;
    }

    private String mapImageTypeToFormat(Integer imageType) {
        return switch (imageType != null ? imageType : -1) {
            case 0 -> "DICOM";
            case 1 -> "JPG";
            case 2 -> "PNG";
            default -> "UNKNOWN";
        };
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getUserId();
        }
        throw new BusinessException("未登录");
    }
}
