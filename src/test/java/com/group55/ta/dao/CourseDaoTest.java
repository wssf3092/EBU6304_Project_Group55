package com.group55.ta.dao;

import com.group55.ta.model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CourseDao.
 */
class CourseDaoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        courseDao = new CourseDao("data/test_courses.txt");
        courseDao.clearAll();
    }

    @AfterEach
    void tearDown() {
        courseDao.clearAll();
    }

    @Test
    void testSaveAndFindById() {
        Course course = new Course("C001", "Software Engineering", "Dr. Smith", "Learn SE principles", 3, 0);

        boolean saved = courseDao.save(course);
        assertTrue(saved, "Course should be saved successfully");

        Course retrieved = courseDao.findById("C001");
        assertNotNull(retrieved, "Course should be discoverable by its string ID");
        assertEquals("Software Engineering", retrieved.getName(), "Course names must perfectly match");
        assertEquals(3, retrieved.getTaNeedCount(), "TA need counts should match definition");
    }

    @Test
    void testFindAll() {
        courseDao.save(new Course("C001", "Course 1", "T1", "Desc1", 2, 0));
        courseDao.save(new Course("C002", "Course 2", "T2", "Desc2", 4, 1));

        List<Course> courses = courseDao.findAll();
        assertNotNull(courses, "Course list shouldn't be null");
        assertTrue(courses.size() >= 2, "Should return all saved courses accurately");
    }
}
