package com.cloudbrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbrain.entity.GenerationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenerationLogMapper extends BaseMapper<GenerationLog> {
}
