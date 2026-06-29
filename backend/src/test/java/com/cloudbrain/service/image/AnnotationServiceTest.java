package com.cloudbrain.service.image;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.TestAuthUtils;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.image.ImageAnnotateRequest;
import com.cloudbrain.dto.response.image.AnnotationVO;
import com.cloudbrain.dto.response.image.ImageVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnotationServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private AnnotationService annotationService;

    private String testImageId;
    private String testAnnotationId;

    @BeforeEach
    void setUpAuth() {
        TestAuthUtils.setupAuth();
    }

    @AfterEach
    void tearDownAuth() {
        TestAuthUtils.clearAuth();
    }

    @BeforeAll
    void setUp() {
        TestAuthUtils.setupAuth();
        MockMultipartFile file = new MockMultipartFile(
                "file", "annotation_test.png", "image/png",
                "annotation-test-content".getBytes());
        ImageVO vo = imageService.upload(file, "PAT_003", null, "MRI", "头部");
        testImageId = vo.getImageId();
        TestAuthUtils.clearAuth();
    }

    @Test
    @Order(1)
    @DisplayName("创建标注")
    void testCreate() {
        ImageAnnotateRequest request = new ImageAnnotateRequest();
        request.setImageId(testImageId);
        request.setAnnotationType("rectangle");
        request.setCoordinates("{\"x\":120,\"y\":80,\"width\":60,\"height\":40}");
        request.setLabel("结节");
        request.setMeasurement("1.2cm×0.8cm");
        request.setDescription("右肺上叶磨玻璃样结节");

        AnnotationVO vo = annotationService.create(request);
        assertNotNull(vo.getAnnotationId());
        assertEquals(testImageId, vo.getImageId());
        assertEquals("rectangle", vo.getAnnotationType());
        assertEquals("结节", vo.getLabel());
        assertNotNull(vo.getCreateTime());

        testAnnotationId = vo.getAnnotationId();
    }

    @Test
    @Order(2)
    @DisplayName("创建标注—不存在的影像应抛异常")
    void testCreateImageNotFound() {
        ImageAnnotateRequest request = new ImageAnnotateRequest();
        request.setImageId("IMG_NOT_EXIST");
        request.setAnnotationType("circle");
        request.setCoordinates("{\"x\":0,\"y\":0}");

        assertThrows(BusinessException.class, () -> annotationService.create(request));
    }

    @Test
    @Order(3)
    @DisplayName("查询影像标注列表")
    void testListByImageId() {
        List<AnnotationVO> annotations = annotationService.listByImageId(testImageId);
        assertNotNull(annotations);
        assertFalse(annotations.isEmpty());
        assertTrue(annotations.stream().anyMatch(a -> testAnnotationId.equals(a.getAnnotationId())));
    }

    @Test
    @Order(4)
    @DisplayName("查询无标注的影像返回空列表")
    void testListByImageIdEmpty() {
        // 再上传一张新影像（无标注）
        MockMultipartFile file = new MockMultipartFile(
                "file", "no_annot.png", "image/png",
                "no-annotation".getBytes());
        ImageVO vo = imageService.upload(file, "PAT_004", null, null, null);

        List<AnnotationVO> annotations = annotationService.listByImageId(vo.getImageId());
        assertNotNull(annotations);
        assertTrue(annotations.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("修改标注")
    void testUpdate() {
        ImageAnnotateRequest request = new ImageAnnotateRequest();
        request.setImageId(testImageId);
        request.setAnnotationType("circle");
        request.setCoordinates("{\"x\":200,\"y\":150,\"r\":30}");
        request.setLabel("钙化灶");
        request.setMeasurement("3.0cm");

        AnnotationVO vo = annotationService.update(testAnnotationId, request);
        assertEquals("circle", vo.getAnnotationType());
        assertEquals("钙化灶", vo.getLabel());
    }

    @Test
    @Order(6)
    @DisplayName("删除标注")
    void testDelete() {
        // 先创建一个临时标注用于删除
        ImageAnnotateRequest createReq = new ImageAnnotateRequest();
        createReq.setImageId(testImageId);
        createReq.setAnnotationType("text");
        createReq.setCoordinates("{\"x\":10,\"y\":10}");
        createReq.setLabel("删除测试");
        AnnotationVO temp = annotationService.create(createReq);

        annotationService.delete(temp.getAnnotationId());
        assertThrows(BusinessException.class, () -> annotationService.delete(temp.getAnnotationId()));
    }

    @Test
    @Order(7)
    @DisplayName("删除不存在的标注应抛异常")
    void testDeleteNotFound() {
        assertThrows(BusinessException.class, () -> annotationService.delete("ANN_NOT_EXIST"));
    }
}
