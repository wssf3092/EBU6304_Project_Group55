package com.group55.ta.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseServlet extends HttpServlet {

    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        String name = viewName;
        if (!name.endsWith(".jsp")) {
            name = name + ".jsp";
        }

        String viewPath = "/WEB-INF/views/" + name;
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    protected void sendJsonResponse(HttpServletResponse response, String jsonBody) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.write(jsonBody == null ? "null" : jsonBody);
            out.flush();
        }
    }
}

