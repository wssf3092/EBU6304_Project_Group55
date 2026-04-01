package com.group55.ta.dao;

import com.group55.ta.model.Application;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDaoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir);
    }

    @Test
    void createAndListByApplicant() {
        ApplicationDao dao = new ApplicationDao();
        Application a = dao.create("TA_001", "CSE101", "I want this course.");

        assertNotNull(a.getApplicationId());
        assertTrue(a.getApplicationId().startsWith("APP_"));
        assertEquals("PENDING", a.getStatus());

        List<Application> forTa = dao.findByApplicant("TA_001");
        assertEquals(1, forTa.size());
    }

    @Test
    void duplicateApplyRejected() {
        ApplicationDao dao = new ApplicationDao();
        dao.create("TA_001", "C1", "one");
        assertThrows(IllegalStateException.class, () -> dao.create("TA_001", "C1", "two"));
    }

    @Test
    void updateStatus() {
        ApplicationDao dao = new ApplicationDao();
        Application a = dao.create("TA_001", "C1", "stmt");
        assertTrue(dao.updateStatus(a.getApplicationId(), Application.Status.ACCEPTED));
        assertEquals("ACCEPTED", dao.findById(a.getApplicationId()).orElseThrow().getStatus());
    }
}
