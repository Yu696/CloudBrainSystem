package com.cloudbrain.service.appointment;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.Doctor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DoctorServiceTest {

    @Autowired
    private DoctorService doctorService;

    @Test
    @DisplayName("获取全部可出诊医生")
    void testListAll() {
        List<Doctor> list = doctorService.listAll();
        assertNotNull(list);
        assertTrue(list.size() >= 10);
    }

    @Test
    @DisplayName("按科室获取医生")
    void testListByDepartment() {
        List<Doctor> list = doctorService.listByDepartment("DEPT_001");
        assertNotNull(list);
        assertFalse(list.isEmpty());
        list.forEach(doc -> assertEquals("DEPT_001", doc.getDepartmentId()));
    }

    @Test
    @DisplayName("获取医生详情")
    void testGetDetail() {
        Doctor doc = doctorService.getDetail("DOC_001");
        assertNotNull(doc);
        assertEquals("DOC_001", doc.getDoctorId());
    }

    @Test
    @DisplayName("获取不存在的医生应抛异常")
    void testGetDetailNotFound() {
        assertThrows(BusinessException.class,
                () -> doctorService.getDetail("NOT_EXIST"));
    }
}
