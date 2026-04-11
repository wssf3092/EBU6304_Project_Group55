package com.group55.ta;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Embedded Tomcat entry point for local startup and QA.
 */
public final class DevServer {
    private DevServer() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String webappPath = new File("src/main/webapp").getAbsolutePath();
        String classesPath = new File("target/classes").getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());

        Context context = tomcat.addWebapp("", webappPath);
        context.setParentClassLoader(DevServer.class.getClassLoader());

        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classesPath, "/"));
        context.setResources(resources);
        tomcat.getConnector();

        tomcat.start();
        System.out.println("TA Recruitment System running at http://127.0.0.1:" + port);
        tomcat.getServer().await();
    }
}
