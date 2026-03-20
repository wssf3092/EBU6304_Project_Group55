package com.group55.ta.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CourseListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("courses", safeFindAllCourses());
        forwardToView(request, response, "course-list");
    }

    @SuppressWarnings("unchecked")
    private List<Object> safeFindAllCourses() throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.CourseDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method findAll = daoClass.getMethod("findAll");
            Object result = findAll.invoke(dao);
            return result == null ? Collections.emptyList() : (List<Object>) result;
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ServletException("加载课程列表失败", e);
        }
    }
}

