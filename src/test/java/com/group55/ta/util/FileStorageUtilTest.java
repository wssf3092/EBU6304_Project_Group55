package com.group55.ta.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileStorageUtil
 * Focuses on baseline text file operations.
 */
class FileStorageUtilTest {

    private final String testFilePath = "data/test_storage.txt";

    @BeforeEach
    void setUp() {
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    @AfterEach
    void tearDown() {
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testWriteAndReadFile() {
        String content = "Hello, World!";
        boolean writeSuccess = FileStorageUtil.writeFile(testFilePath, content);
        assertTrue(writeSuccess, "Write file should return true upon success");

        String readContent = FileStorageUtil.readFile(testFilePath);
        assertEquals(content, readContent, "Read content should exactly match written content");
    }

    @Test
    void testAppendToFile() {
        FileStorageUtil.writeFile(testFilePath, "Line 1");

        boolean appendSuccess = FileStorageUtil.appendToFile(testFilePath, "\nLine 2");
        assertTrue(appendSuccess, "Append file should return true");

        String readContent = FileStorageUtil.readFile(testFilePath);
        assertEquals("Line 1\nLine 2", readContent, "Content should contain both lines");
    }

    @Test
    void testReadLines() {
        FileStorageUtil.writeFile(testFilePath, "Line 1\nLine 2\nLine 3");

        List<String> lines = FileStorageUtil.readLines(testFilePath);

        assertNotNull(lines, "Lines list should not be null");
        assertEquals(3, lines.size(), "Should read exactly 3 valid lines");
        assertEquals("Line 1", lines.get(0), "First line should map correctly");
        assertEquals("Line 3", lines.get(2), "Third line should map correctly");
    }

    @Test
    void testFileNotExists() {
        String nonExistentPath = "data/does_not_exist_file.txt";

        String content = FileStorageUtil.readFile(nonExistentPath);
        assertNull(content, "Reading non-existent file should return null to prevent crashes");

        List<String> lines = FileStorageUtil.readLines(nonExistentPath);
        assertNull(lines, "Reading lines of non-existent file should safely return null");
    }
}
