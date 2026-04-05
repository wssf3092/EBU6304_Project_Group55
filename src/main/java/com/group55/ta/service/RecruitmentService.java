package com.group55.ta.service;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Application.Status;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.EntityHydrator;
import com.group55.ta.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Course / application flows (Step 3–6 baseline).
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
     * MO 查看某课程申请人列表（含课程/教师/申请人姓名）。
     */
    public List<Application> listApplicationsEnrichedForCourseMoView(String courseId) {
        List<Application> apps = applicationDao.findByCourseId(courseId);
        EntityHydrator.enrichApplications(apps, courseDao, userDao);
        EntityHydrator.enrichApplicationApplicants(apps, userDao);
        return apps;
    }

    /**
     * Step 6：接受/拒绝 + 可选审核备注；仅 {@link Status#PENDING} 可审。
     */
    public void reviewApplication(User moUser, String applicationId, boolean accept, String note) {
        if (moUser == null || ValidationUtil.isBlank(applicationId)) {
            throw new IllegalArgumentException("参数不完整。");
        }
        Application app = applicationDao.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在。"));
        if (app.getStatusEnum() != Status.PENDING) {
            throw new IllegalStateException("该申请已审核。");
        }
        String courseId = app.getCourseId();
        Course course = courseDao.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在。");
        }
        if (!moUser.getUserId().equals(course.getTeacher())) {
            throw new IllegalStateException("无权限审核该申请。");
        }
        if (accept) {
            int accepted = countAccepted(applicationDao.findByCourseId(courseId));
            if (accepted >= course.getTaNeedCount()) {
                throw new IllegalStateException("该课程 TA 名额已满。");
            }
            applicationDao.updateStatusAndReview(applicationId, Status.ACCEPTED, note);
        } else {
            applicationDao.updateStatusAndReview(applicationId, Status.REJECTED, note);
        }
    }

    /**
     * 兼容旧调用（无备注）。
     */
    public void reviewCourseApplication(User moUser, String courseId, String applicationId, boolean accept) {
        Application app = applicationDao.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在。"));
        if (!courseId.equals(app.getCourseId())) {
            throw new IllegalArgumentException("申请与课程不匹配。");
        }
        reviewApplication(moUser, applicationId, accept, null);
    }

    public Application getApplicationForMoReview(User moUser, String applicationId) {
        if (moUser == null || moUser.getRoleEnum() != Role.MO || ValidationUtil.isBlank(applicationId)) {
            return null;
        }
        return applicationDao.findById(applicationId)
                .filter(app -> {
                    Course c = courseDao.findById(app.getCourseId());
                    return c != null && moUser.getUserId().equals(c.getTeacher());
                })
                .map(app -> {
                    List<Application> one = Collections.singletonList(app);
                    EntityHydrator.enrichApplications(one, courseDao, userDao);
                    EntityHydrator.enrichApplicationApplicants(one, userDao);
                    return app;
                })
                .orElse(null);
    }

    public User findApplicantUser(String applicantId) {
        if (ValidationUtil.isBlank(applicantId)) {
            return null;
        }
        return userDao.findById(applicantId).orElse(null);
    }

    public List<User> listUsersForAdmin() {
        List<User> users = new ArrayList<>(userDao.listAll());
        users.sort(Comparator
                .comparing(User::getRole, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(User::getUserId, Comparator.nullsLast(String::compareTo)));
        return users;
    }

    public void setUserActive(User admin, String targetUserId, boolean active) {
        if (admin == null || admin.getRoleEnum() != Role.ADMIN) {
            throw new IllegalStateException("无权限管理用户。");
        }
        if (ValidationUtil.isBlank(targetUserId)) {
            throw new IllegalArgumentException("用户 ID 不能为空。");
        }
        if (targetUserId.equals(admin.getUserId())) {
            throw new IllegalArgumentException("不能修改自己的账号状态。");
        }
        if (!userDao.setActive(targetUserId, active)) {
            throw new IllegalArgumentException("用户不存在。");
        }
    }

    private int countAccepted(List<Application> apps) {
        int n = 0;
        if (apps == null) {
            return 0;
        }
        for (Application a : apps) {
            if (a != null && a.getStatusEnum() == Status.ACCEPTED) {
                n++;
            }
        }
        return n;
    }
}
