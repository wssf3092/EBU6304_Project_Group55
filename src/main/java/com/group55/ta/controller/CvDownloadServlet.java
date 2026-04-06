package com.group55.ta.controller;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.CvFileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Secure CV download endpoint.
 */
@WebServlet("/files/cv")
public class CvDownloadServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User requester = currentUser(request);
        String userId = request.getParameter("userId");
        if (Role.TA.name().equalsIgnoreCase(requester.getRole()) && !requester.getUserId().equals(userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path file = CvFileUtil.findCv(userId).orElse(null);
        if (file == null || !Files.exists(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(CvFileUtil.contentType(file));
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(file.getFileName().toString(), StandardCharsets.UTF_8.name()));
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }
}
