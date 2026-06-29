package com.cloudbrain.service.image.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.image.ImageAnnotateRequest;
import com.cloudbrain.dto.response.image.AnnotationVO;
import com.cloudbrain.entity.Annotation;
import com.cloudbrain.entity.User;
import com.cloudbrain.mapper.AnnotationMapper;
import com.cloudbrain.mapper.MedicalImageMapper;
import com.cloudbrain.service.image.AnnotationService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnotationServiceImpl implements AnnotationService {

    private final AnnotationMapper annotationMapper;
    private final MedicalImageMapper medicalImageMapper;

    @Override
    @Transactional
    public AnnotationVO create(ImageAnnotateRequest request) {
        // 校验影像存在
        if (medicalImageMapper.selectOne(
                new LambdaQueryWrapper<com.cloudbrain.entity.MedicalImage>()
                        .eq(com.cloudbrain.entity.MedicalImage::getImageId, request.getImageId())) == null) {
            throw new BusinessException("影像不存在");
        }

        Annotation annotation = new Annotation();
        annotation.setAnnotationId(UUIDUtil.generateAnnotationId());
        annotation.setImageId(request.getImageId());
        annotation.setAnnotatorId(getCurrentUserId());
        annotation.setAnnotationType(request.getAnnotationType());
        annotation.setCoordinates(request.getCoordinates());
        annotation.setLabel(request.getLabel());
        annotation.setMeasurement(request.getMeasurement());
        annotation.setDescription(request.getDescription());
        annotationMapper.insert(annotation);

        return AnnotationVO.builder()
                .annotationId(annotation.getAnnotationId())
                .imageId(annotation.getImageId())
                .annotatorId(annotation.getAnnotatorId())
                .annotationType(annotation.getAnnotationType())
                .coordinates(annotation.getCoordinates())
                .label(annotation.getLabel())
                .measurement(annotation.getMeasurement())
                .description(annotation.getDescription())
                .createTime(annotation.getCreateTime())
                .updateTime(annotation.getUpdateTime())
                .build();
    }

    @Override
    public List<AnnotationVO> listByImageId(String imageId) {
        List<Annotation> annotations = annotationMapper.selectList(
                new LambdaQueryWrapper<Annotation>().eq(Annotation::getImageId, imageId));
        return annotations.stream().map(a -> AnnotationVO.builder()
                .annotationId(a.getAnnotationId())
                .imageId(a.getImageId())
                .annotatorId(a.getAnnotatorId())
                .annotationType(a.getAnnotationType())
                .coordinates(a.getCoordinates())
                .label(a.getLabel())
                .measurement(a.getMeasurement())
                .description(a.getDescription())
                .createTime(a.getCreateTime())
                .updateTime(a.getUpdateTime())
                .build()).toList();
    }

    @Override
    @Transactional
    public void delete(String annotationId) {
        Annotation annotation = annotationMapper.selectOne(
                new LambdaQueryWrapper<Annotation>().eq(Annotation::getAnnotationId, annotationId));
        if (annotation == null) {
            throw new BusinessException("标注不存在");
        }
        annotationMapper.deleteById(annotation.getId());
    }

    @Override
    @Transactional
    public AnnotationVO update(String annotationId, ImageAnnotateRequest request) {
        Annotation annotation = annotationMapper.selectOne(
                new LambdaQueryWrapper<Annotation>().eq(Annotation::getAnnotationId, annotationId));
        if (annotation == null) {
            throw new BusinessException("标注不存在");
        }
        annotation.setAnnotationType(request.getAnnotationType());
        annotation.setCoordinates(request.getCoordinates());
        annotation.setLabel(request.getLabel());
        annotation.setMeasurement(request.getMeasurement());
        annotation.setDescription(request.getDescription());
        annotationMapper.updateById(annotation);

        return AnnotationVO.builder()
                .annotationId(annotation.getAnnotationId())
                .imageId(annotation.getImageId())
                .annotatorId(annotation.getAnnotatorId())
                .annotationType(annotation.getAnnotationType())
                .coordinates(annotation.getCoordinates())
                .label(annotation.getLabel())
                .measurement(annotation.getMeasurement())
                .description(annotation.getDescription())
                .createTime(annotation.getCreateTime())
                .updateTime(annotation.getUpdateTime())
                .build();
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user.getUserId();
        }
        throw new BusinessException("未登录");
    }
}
