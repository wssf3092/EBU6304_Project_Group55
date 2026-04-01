package com.group55.ta.util;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Course;

import java.util.List;

/**
 * Fills transient display fields for JSPs (not persisted).
 */
public final class EntityHydrator {
    private EntityHydrator() {
    }

    public static void enrichCourse(Course course, UserDao users, ApplicationDao applications) {
        if (course == null) {
            return;
        }
        users.findById(course.getTeacher()).ifPresent(mo -> course.setTeacherName(mo.getName()));
        course.setApplicantCount(applications.findByCourseId(course.getCourseId()).size());
    }

    public static void enrichCourses(List<Course> courses, UserDao users, ApplicationDao applications) {
        if (courses == null) {
            return;
        }
        for (Course c : courses) {
            enrichCourse(c, users, applications);
        }
    }

    public static void enrichApplications(List<Application> apps, CourseDao courses, UserDao users) {
        if (apps == null) {
            return;
        }
        for (Application a : apps) {
            Course c = courses.findById(a.getCourseId());
            if (c != null) {
                a.setCourseName(c.getName());
                users.findById(c.getTeacher()).ifPresent(mo -> a.setTeacherName(mo.getName()));
            }
        }
    }
}
