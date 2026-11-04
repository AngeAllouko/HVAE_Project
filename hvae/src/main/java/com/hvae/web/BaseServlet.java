package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.DbBootstrap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Shared helpers: view forwarding and a database-readiness guard. */
@SuppressWarnings("serial")
abstract class BaseServlet extends HttpServlet {

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view, String title, String nav)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", title);
        req.setAttribute("nav", nav);
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** Returns true if the database is usable; otherwise renders the setup-help page and returns false. */
    protected boolean requireDb(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Db.isReady() || "1".equals(req.getParameter("retry"))) DbBootstrap.run();
        if (Db.isReady()) return true;
        req.setAttribute("dbError", Db.getInitError());
        render(req, resp, "dberror", "Database not available", "");
        return false;
    }

    protected static Integer parseInt(String s) {
        try { return s == null ? null : Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
