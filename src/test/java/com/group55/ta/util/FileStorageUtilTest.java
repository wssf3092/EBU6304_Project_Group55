package com.group55.ta.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for FileStorageUtil
 * Focuses on baseline text file operations.
 */
public class FileStorageUtilTest {

    private final String testFilePath = "data/test_storage.txt";

    @Before
    public void setUp() {
        // Ensure clean state before each test
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    @After
    public void tearDown() {
        // Cleanup after tests
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testWriteAndReadFile() {
        String content = "Hello, World!";
        // Assuming FileStorageUtil.writeFile(path, content) exists
        boolean writeSuccess = FileStorageUtil.writeFile(testFilePath, content);
        assertTrue("Write file should return true upon success", writeSuccess);

        // Assuming FileStorageUtil.readFile(path) exists
        String readContent = FileStorageUtil.readFile(testFilePath);
        assertEquals("Read content should exactly match written content", content, readContent);
    }

    @Test
    public void testAppendToFile() {
        FileStorageUtil.writeFile(testFilePath, "Line 1");
        
        // Assuming FileStorageUtil.appendToFile(path, content) exists
        boolean appendSuccess = FileStorageUtil.appendToFile(testFilePath, "\nLine 2");
        assertTrue("Append file should return true", appendSuccess);

        String readContent = FileStorageUtil.readFile(testFilePath);
        assertEquals("Content should contain both lines", "Line 1\nLine 2", readContent);
    }

    @Test
    public void testReadLines() {
        FileStorageUtil.writeFile(testFilePath, "Line 1\nLine 2\nLine 3");
        
        // Assuming FileStorageUtil.readLines(path) exists
        List<String> lines = FileStorageUtil.readLines(testFilePath);
        
        assertNotNull("Lines list should not be null", lines);
        assertEquals("Should read exactly 3 valid lines", 3, lines.size());
        assertEquals("First line should map correctly", "Line 1", lines.get(0));
        assertEquals("Third line should map correctly", "Line 3", lines.get(2));
    }

    @Test
    public void testFileNotExists() {
        String nonExistentPath = "data/does_not_exist_file.txt";
        
        // Exception handling: should return null gracefully rather than crash
        String content = FileStorageUtil.readFile(nonExistentPath);
        assertNull("Reading non-existent file should return null to prevent crashes", content);
        
        List<String> lines = FileStorageUtil.readLines(nonExistentPath);
        assertNull("Reading lines of non-existent file should safely return null", lines);
    }
}
