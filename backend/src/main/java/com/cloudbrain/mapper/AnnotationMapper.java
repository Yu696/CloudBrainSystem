package com.cloudbrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbrain.entity.Annotation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnotationMapper extends BaseMapper<Annotation> {
}
