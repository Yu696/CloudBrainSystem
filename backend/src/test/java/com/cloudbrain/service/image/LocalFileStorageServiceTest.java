package com.cloudbrain.service.image;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 本地文件存储服务测试。
 * @CloudbrainTest 注解通过 TestPropertySource 将 storage.type 设为 local。
 * 每个测试后清理创建的文件。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalFileStorageServiceTest {

    @Autowired
    private StorageService storageService;

    private final List<String> createdPaths = new ArrayList<>();

    @AfterEach
    void cleanup() {
        for (String path : createdPaths) {
            try {
                Files.deleteIfExists(Paths.get("./data/cloudbrain/images", path));
            } catch (IOException ignored) {
            }
        }
        createdPaths.clear();
    }

    // ======================== 存储 ========================

    @Test
    @Order(1)
    @DisplayName("存储文件 — 正常创建并返回路径")
    void testStore() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png",
                "test image content".getBytes());

        String path = storageService.store(file, "IMG_test_store_001");
        assertNotNull(path);
        assertTrue(path.contains("IMG_test_store_001"));
        createdPaths.add(path);

        // 验证文件确实存在
        assertTrue(Files.exists(Paths.get("./data/cloudbrain/images", path)));
    }

    @Test
    @Order(2)
    @DisplayName("存储文件 — 无扩展名的文件")
    void testStoreNoExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noext", "application/octet-stream",
                "binary content".getBytes());

        String path = storageService.store(file, "IMG_test_noext");
        assertNotNull(path);
        createdPaths.add(path);
    }

    // ======================== 读取 ========================

    @Test
    @Order(3)
    @DisplayName("读取文件 — 获取字节数组")
    void testRetrieve() throws IOException {
        byte[] content = "hello world".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "read_test.txt", "text/plain", content);

        String path = storageService.store(file, "IMG_test_retrieve");
        createdPaths.add(path);

        byte[] retrieved = storageService.retrieve(path);
        assertArrayEquals(content, retrieved);
    }

    @Test
    @Order(4)
    @DisplayName("读取文件 — 获取输入流")
    void testRetrieveAsStream() throws IOException {
        byte[] content = "stream test content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "stream_test.txt", "text/plain", content);

        String path = storageService.store(file, "IMG_test_stream");
        createdPaths.add(path);

        try (InputStream in = storageService.retrieveAsStream(path)) {
            byte[] fromStream = in.readAllBytes();
            assertArrayEquals(content, fromStream);
        }
    }

    @Test
    @Order(5)
    @DisplayName("读取文件 — 不存在的文件抛异常")
    void testRetrieveNotFound() {
        assertThrows(BusinessException.class,
                () -> storageService.retrieve("9999/99/99/non_existent_file.png"));
    }

    // ======================== 删除 ========================

    @Test
    @Order(6)
    @DisplayName("删除文件 — 正常删除")
    void testDelete() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "delete_test.txt", "text/plain",
                "to be deleted".getBytes());

        String path = storageService.store(file, "IMG_test_delete");
        createdPaths.add(path);
        assertTrue(Files.exists(Paths.get("./data/cloudbrain/images", path)));

        storageService.delete(path);

        // 验证文件已被删除
        assertFalse(Files.exists(Path.of("./data/cloudbrain/images", path)));
        createdPaths.remove(path); // 避免 @AfterEach 重复删除
    }

    @Test
    @Order(7)
    @DisplayName("删除文件 — 不存在的文件不抛异常")
    void testDeleteNonExistent() {
        assertDoesNotThrow(() -> storageService.delete("9999/99/99/non_existent_file.png"));
    }

    // ======================== 获取访问URL ========================

    @Test
    @Order(8)
    @DisplayName("获取访问URL — 返回原路径")
    void testGetAccessUrl() {
        String url = storageService.getAccessUrl("2026/01/01/test.png");
        assertEquals("2026/01/01/test.png", url);
    }

    // ======================== 文件大小 ========================

    @Test
    @Order(9)
    @DisplayName("获取文件大小 — 返回正确大小")
    void testGetFileSize() {
        byte[] content = new byte[1024]; // 1KB
        MockMultipartFile file = new MockMultipartFile(
                "file", "size_test.bin", "application/octet-stream", content);

        String path = storageService.store(file, "IMG_test_size");
        createdPaths.add(path);

        long size = storageService.getFileSize(path);
        assertEquals(1024, size);
    }

    @Test
    @Order(10)
    @DisplayName("获取文件大小 — 不存在的文件返回0")
    void testGetFileSizeNonExistent() {
        long size = storageService.getFileSize("9999/99/99/non_existent_file.png");
        assertEquals(0, size);
    }
}
