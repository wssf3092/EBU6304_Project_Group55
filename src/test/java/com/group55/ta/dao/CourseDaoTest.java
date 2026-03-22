package com.group55.ta.dao;

import com.group55.ta.model.Course;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for CourseDao.
 */
public class CourseDaoTest {

    private CourseDao courseDao;

    @Before
    public void setUp() {
        courseDao = new CourseDao("data/test_courses.txt");
        courseDao.clearAll();
    }
    
    @After
    public void tearDown() {
        courseDao.clearAll();
    }

    @Test
    public void testSaveAndFindById() {
        Course course = new Course("C001", "Software Engineering", "Dr. Smith", "Learn SE principles", 3, 0);
        
        boolean saved = courseDao.save(course);
        assertTrue("Course should be saved successfully", saved);

        Course retrieved = courseDao.findById("C001");
        assertNotNull("Course should be discoverable by its string ID", retrieved);
        assertEquals("Course names must perfectly match", "Software Engineering", retrieved.getName());
        assertEquals("TA need counts should match definition", 3, retrieved.getTaNeedCount());
    }

    @Test
    public void testFindAll() {
        courseDao.save(new Course("C001", "Course 1", "T1", "Desc1", 2, 0));
        courseDao.save(new Course("C002", "Course 2", "T2", "Desc2", 4, 1));

        List<Course> courses = courseDao.findAll();
        assertNotNull("Course list shouldn't be null", courses);
        assertTrue("Should return all saved courses accurately", courses.size() >= 2);
    }
}
