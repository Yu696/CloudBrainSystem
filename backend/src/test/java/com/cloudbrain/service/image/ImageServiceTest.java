package com.cloudbrain.service.image;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.TestAuthUtils;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.response.image.ImageVO;
import com.cloudbrain.entity.MedicalImage;
import com.cloudbrain.mapper.MedicalImageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private MedicalImageMapper medicalImageMapper;

    private static String testImageId;
    private static final List<String> createdImageIds = new ArrayList<>();
    private static MedicalImageMapper staticMapper;

    @BeforeEach
    void setUpAuth() {
        TestAuthUtils.setupAuth();
        if (staticMapper == null) {
            staticMapper = medicalImageMapper;
        }
    }

    @AfterEach
    void tearDownAuth() {
        TestAuthUtils.clearAuth();
    }

    @AfterAll
    static void cleanup() {
        if (staticMapper != null && !createdImageIds.isEmpty()) {
            createdImageIds.forEach(imageId ->
                    staticMapper.delete(new LambdaQueryWrapper<MedicalImage>()
                            .eq(MedicalImage::getImageId, imageId)));
        }
    }

    @Test
    @Order(1)
    @DisplayName("上传影像—PNG 格式")
    void testUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test_image.png", "image/png",
                "fake-png-content".getBytes());

        ImageVO vo = imageService.upload(file, "PAT_001", null, "CT", "胸部");
        assertNotNull(vo.getImageId());
        assertEquals("test_image.png", vo.getImageName());
        assertEquals(2, vo.getImageType()); // PNG
        assertEquals("PNG", vo.getImageTypeName());
        assertEquals("CT", vo.getModality());
        assertEquals("胸部", vo.getBodyPart());
        assertEquals("PAT_001", vo.getPatientId());
        assertEquals(16, vo.getFileSize());
        assertNotNull(vo.getUploadTime());

        testImageId = vo.getImageId();
        createdImageIds.add(vo.getImageId());
    }

    @Test
    @Order(2)
    @DisplayName("上传影像—JPG 格式")
    void testUploadJpg() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "xray_chest.jpg", "image/jpeg",
                "fake-jpg-content".getBytes());

        ImageVO vo = imageService.upload(file, "PAT_002", null, "X光", "胸部");
        assertEquals(1, vo.getImageType()); // JPG
        assertEquals("JPG", vo.getImageTypeName());
        createdImageIds.add(vo.getImageId());
    }

    @Test
    @Order(3)
    @DisplayName("上传不支持的文件格式应抛异常")
    void testUploadInvalidFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf",
                "fake-pdf".getBytes());

        assertThrows(BusinessException.class, () ->
                imageService.upload(file, "PAT_001", null, null, null));
    }

    @Test
    @Order(4)
    @DisplayName("查询影像详情")
    void testGetInfo() {
        assertNotNull(testImageId, "需要先执行上传测试");
        ImageVO vo = imageService.getInfo(testImageId);
        assertEquals(testImageId, vo.getImageId());
        assertEquals("test_image.png", vo.getImageName());
    }

    @Test
    @Order(5)
    @DisplayName("查询不存在的影像应抛异常")
    void testGetInfoNotFound() {
        assertThrows(BusinessException.class, () -> imageService.getInfo("IMG_NOT_EXIST"));
    }

    @Test
    @Order(6)
    @DisplayName("影像列表分页查询")
    void testList() {
        PageResult<ImageVO> result = imageService.list(null, null, null, null, false, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getRecords().get(0).getImageId());
    }

    @Test
    @Order(7)
    @DisplayName("影像预览")
    void testPreview() {
        assertNotNull(testImageId, "需要先执行上传测试");
        byte[] bytes = imageService.preview(testImageId);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    @Order(8)
    @DisplayName("影像对比")
    void testCompare() {
        // 上传第二张图片用于对比
        MockMultipartFile file = new MockMultipartFile(
                "file", "ct_brain.png", "image/png",
                "second-image".getBytes());
        ImageVO vo2 = imageService.upload(file, "PAT_001", null, "CT", "头部");
        createdImageIds.add(vo2.getImageId());

        var result = imageService.compare(testImageId, vo2.getImageId());
        assertEquals(testImageId, result.get("imageId1"));
        assertEquals(vo2.getImageId(), result.get("imageId2"));
        assertTrue(result.get("previewUrl1").contains(testImageId));
    }

    @Test
    @Order(9)
    @DisplayName("格式转换")
    void testConvert() {
        assertNotNull(testImageId, "需要先执行上传测试");
        var result = imageService.convert(testImageId, "PNG");
        assertEquals(testImageId, result.getImageId());
        assertEquals("PNG", result.getTargetFormat());
        assertEquals(1, result.getStatus());
    }

    @Test
    @Order(10)
    @DisplayName("删除影像—逻辑删除")
    void testDelete() {
        assertNotNull(testImageId, "需要先执行上传测试");
        imageService.delete(testImageId);

        ImageVO vo = imageService.getInfo(testImageId);
        assertEquals(0, vo.getStatus());
    }
}
