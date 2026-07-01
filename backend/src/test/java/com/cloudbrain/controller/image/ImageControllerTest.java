package com.cloudbrain.controller.image;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.image.AnnotationVO;
import com.cloudbrain.dto.response.image.ConvertResultVO;
import com.cloudbrain.dto.response.image.ImageVO;
import com.cloudbrain.service.image.AnnotationService;
import com.cloudbrain.service.image.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ImageControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ImageController 单元测试")
class ImageControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.image", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @MockBean
    private AnnotationService annotationService;

    @Nested
    @DisplayName("影像管理")
    class ImageManagement {
        @Test
        @DisplayName("查询影像信息")
        void testInfo() throws Exception {
            ImageVO vo = ImageVO.builder().imageId("IMG_test001").patientId("PAT_001").build();
            when(imageService.getInfo("IMG_test001")).thenReturn(vo);

            mockMvc.perform(get("/api/image/info").param("imageId", "IMG_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.imageId").value("IMG_test001"));
        }

        @Test
        @DisplayName("影像列表分页查询")
        void testList() throws Exception {
            ImageVO vo = ImageVO.builder().imageId("IMG_test001").build();
            PageResult<ImageVO> page = new PageResult<>();
            page.setRecords(List.of(vo));
            page.setTotal(1L);
            when(imageService.list(isNull(), isNull(), isNull(), isNull(), isNull(), eq(1), eq(10)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/image/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("删除影像")
        void testDelete() throws Exception {
            doNothing().when(imageService).delete("IMG_test001");

            mockMvc.perform(delete("/api/image/delete").param("imageId", "IMG_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("影像对比")
        void testCompare() throws Exception {
            when(imageService.compare("IMG_001", "IMG_002"))
                    .thenReturn(Map.of("diffUrl", "/compare/result.png"));

            mockMvc.perform(post("/api/image/compare")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"imageId1\":\"IMG_001\",\"imageId2\":\"IMG_002\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("格式转换")
        void testConvert() throws Exception {
            ConvertResultVO vo = ConvertResultVO.builder()
                    .outputPath("/converted/img.jpg").build();
            when(imageService.convert("IMG_test001", "jpg")).thenReturn(vo);

            mockMvc.perform(post("/api/image/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"imageId\":\"IMG_test001\",\"targetFormat\":\"jpg\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("影像标注")
    class Annotation {
        @Test
        @DisplayName("创建标注")
        void testCreateAnnotation() throws Exception {
            AnnotationVO vo = AnnotationVO.builder()
                    .annotationId("ANN_new001").imageId("IMG_test001").build();
            when(annotationService.create(any())).thenReturn(vo);

            mockMvc.perform(post("/api/image/annotate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"imageId\":\"IMG_test001\",\"annotationType\":\"circle\",\"coordinates\":\"100,100,50\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.annotationId").value("ANN_new001"));
        }

        @Test
        @DisplayName("查询标注列表")
        void testAnnotateList() throws Exception {
            AnnotationVO vo = AnnotationVO.builder()
                    .annotationId("ANN_001").imageId("IMG_test001").build();
            when(annotationService.listByImageId("IMG_test001")).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/image/annotate/list").param("imageId", "IMG_test001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除标注")
        void testDeleteAnnotation() throws Exception {
            doNothing().when(annotationService).delete("ANN_001");

            mockMvc.perform(delete("/api/image/annotate/delete").param("annotationId", "ANN_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("影像不存在")
        void testInfoNotFound() throws Exception {
            when(imageService.getInfo("IMG_NOT_EXIST"))
                    .thenThrow(new BusinessException("影像不存在"));

            mockMvc.perform(get("/api/image/info").param("imageId", "IMG_NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("影像不存在"));
        }
    }
}
