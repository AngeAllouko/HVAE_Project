package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.ScenarioDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/home")
@SuppressWarnings("serial")
public class HomeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        showForm(req, resp);
    }

    static void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection c = Db.connection()) {
            req.setAttribute("scenarios", ScenarioDao.listPredefined(c));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("pageTitle", "Evaluate a response");
        req.setAttribute("nav", "home");
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
