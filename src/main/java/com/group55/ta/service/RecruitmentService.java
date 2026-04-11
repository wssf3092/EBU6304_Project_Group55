package com.group55.ta.service;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.User;
import com.group55.ta.util.EntityHydrator;
import com.group55.ta.util.ValidationUtil;

import java.util.List;

/**
 * Course / application flows (Step 3 baseline, Course 过渡模型).
 */
public class RecruitmentService {
    private final CourseDao courseDao = new CourseDao();
    private final ApplicationDao applicationDao = new ApplicationDao();
    private final UserDao userDao = new UserDao();

    public List<Course> listAllCoursesEnriched() {
        List<Course> courses = courseDao.findAll();
        EntityHydrator.enrichCourses(courses, userDao, applicationDao);
        return courses;
    }

    public List<Application> listApplicationsEnrichedForApplicant(String applicantId) {
        List<Application> apps = applicationDao.findByApplicant(applicantId);
        EntityHydrator.enrichApplications(apps, courseDao, userDao);
        return apps;
    }

    public List<Course> listCoursesEnrichedForMo(String moUserId) {
        List<Course> courses = courseDao.findByTeacher(moUserId);
        EntityHydrator.enrichCourses(courses, userDao, applicationDao);
        return courses;
    }

    public List<Course> listAllCoursesEnrichedForAdmin() {
        return listAllCoursesEnriched();
    }

    public Course findCourse(String courseId) {
        return courseDao.findById(courseId);
    }

    public Application applyForCourse(String applicantId, String courseId, String statement) {
        if (ValidationUtil.isBlank(statement)) {
            throw new IllegalArgumentException("申请陈述不能为空。");
        }
        return applicationDao.create(applicantId, courseId, ValidationUtil.trim(statement));
    }

    public List<Application> listApplicationsForCourse(String courseId) {
        return applicationDao.findByCourseId(courseId);
    }

    /**
     * MO 审核：通过 / 拒绝；通过时检查名额。
     */
    public void reviewCourseApplication(User moUser, String courseId, String applicationId, boolean accept) {
        if (moUser == null || ValidationUtil.isBlank(courseId) || ValidationUtil.isBlank(applicationId)) {
            throw new IllegalArgumentException("参数不完整。");
        }
        Course course = courseDao.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在。");
        }
        if (!moUser.getUserId().equals(course.getTeacher())) {
            throw new IllegalStateException("无权限管理该课程。");
        }
        Application app = applicationDao.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在。"));
        if (!courseId.equals(app.getCourseId())) {
            throw new IllegalArgumentException("申请与课程不匹配。");
        }
        if (accept) {
            int accepted = countAccepted(applicationDao.findByCourseId(courseId));
            if (accepted >= course.getTaNeedCount()) {
                throw new IllegalStateException("该课程 TA 名额已满。");
            }
            applicationDao.updateStatus(applicationId, Application.Status.ACCEPTED);
        } else {
            applicationDao.updateStatus(applicationId, Application.Status.REJECTED);
        }
    }

    private int countAccepted(List<Application> apps) {
        int n = 0;
        if (apps == null) {
            return 0;
        }
        for (Application a : apps) {
            if (a != null && a.getStatusEnum() == Application.Status.ACCEPTED) {
                n++;
            }
        }
        return n;
    }
}
