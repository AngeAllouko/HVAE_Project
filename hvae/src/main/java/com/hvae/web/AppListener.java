package com.hvae.web;

import com.hvae.db.DbBootstrap;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Initialises the database when the web application starts. */
@WebListener
public class AppListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DbBootstrap.run();
    }
}
