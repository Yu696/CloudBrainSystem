package com.cloudbrain.service.ai;

import com.cloudbrain.entity.DiseaseKnowledge;

import java.util.List;

/**
 * 疾病知识库服务
 */
public interface DiseaseKbService {

    /** 关键词搜索疾病（按疾病名称模糊匹配） */
    List<DiseaseKnowledge> search(String keyword);

    /** 查询所有启用的疾病条目 */
    List<DiseaseKnowledge> listAll();

    /** 根据疾病ID查询 */
    DiseaseKnowledge getByDiseaseId(String diseaseId);

    /** 新增疾病条目 */
    DiseaseKnowledge create(DiseaseKnowledge disease);

    /** 更新疾病条目 */
    DiseaseKnowledge update(DiseaseKnowledge disease);

    /** 删除疾病条目 */
    void delete(String diseaseId);
}
