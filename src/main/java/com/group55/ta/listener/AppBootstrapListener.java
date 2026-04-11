package com.group55.ta.listener;

import com.group55.ta.util.AppPaths;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Ensures local data folders exist when the app starts.
 */
@WebListener
public class AppBootstrapListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppPaths.getDataRoot();
    }
}
