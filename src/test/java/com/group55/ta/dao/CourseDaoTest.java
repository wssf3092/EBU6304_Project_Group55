package com.group55.ta.dao;

import com.group55.ta.model.Course;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseDaoTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir);
    }

    @Test
    void saveAndFindById() {
        CourseDao dao = new CourseDao();
        Course c = new Course();
        c.setCourseId("CSE101");
        c.setName("Software Engineering");
        c.setTeacher("MO_001");
        c.setDescription("Desc");
        c.setTaNeedCount(3);
        c.setCurrentTaCount(0);

        assertTrue(dao.save(c));
        Course loaded = dao.findById("CSE101");
        assertNotNull(loaded);
        assertEquals("Software Engineering", loaded.getName());
        assertEquals(3, loaded.getTaNeedCount());
    }

    @Test
    void findByTeacher() {
        CourseDao dao = new CourseDao();
        Course c1 = new Course();
        c1.setCourseId("C1");
        c1.setName("N1");
        c1.setTeacher("MO_001");
        c1.setDescription("");
        c1.setTaNeedCount(1);
        c1.setCurrentTaCount(0);
        dao.save(c1);

        Course c2 = new Course();
        c2.setCourseId("C2");
        c2.setName("N2");
        c2.setTeacher("MO_002");
        c2.setDescription("");
        c2.setTaNeedCount(2);
        c2.setCurrentTaCount(0);
        dao.save(c2);

        List<Course> forMo1 = dao.findByTeacher("MO_001");
        assertEquals(1, forMo1.size());
        assertEquals("C1", forMo1.get(0).getCourseId());
    }
}
