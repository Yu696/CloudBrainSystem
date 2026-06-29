package com.cloudbrain.service.pharmacy;

import com.cloudbrain.dto.request.pharmacy.DrugAddRequest;
import com.cloudbrain.dto.request.pharmacy.DrugUpdateRequest;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.pharmacy.DrugVO;

public interface DrugService {

    /** 新增药品 */
    String add(DrugAddRequest request);

    /** 修改药品 */
    void update(DrugUpdateRequest request);

    /** 删除药品（逻辑删除，status=0） */
    void delete(String drugId);

    /** 搜索药品（分页） */
    PageResult<DrugVO> search(String keyword, String category, Integer prescriptionType, int page, int pageSize);

    /** 药品详情 */
    DrugVO getDetail(String drugId);
}
