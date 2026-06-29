package com.cloudbrain.service.appointment;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Test
    @DisplayName("获取全部科室列表")
    void testListAll() {
        List<Department> list = departmentService.listAll();
        assertNotNull(list);
        assertTrue(list.size() >= 12);
    }

    @Test
    @DisplayName("按分类获取科室")
    void testListByCategory() {
        List<Department> list = departmentService.listByCategory("内科");
        assertNotNull(list);
        assertFalse(list.isEmpty());
        list.forEach(dept -> assertEquals("内科", dept.getCategory()));
    }

    @Test
    @DisplayName("根据ID获取科室详情")
    void testGetByDepartmentId() {
        Department dept = departmentService.getByDepartmentId("DEPT_001");
        assertNotNull(dept);
        assertEquals("内科", dept.getName());
    }

    @Test
    @DisplayName("获取不存在的科室应抛异常")
    void testGetByDepartmentIdNotFound() {
        assertThrows(BusinessException.class,
                () -> departmentService.getByDepartmentId("NOT_EXIST"));
    }
}
