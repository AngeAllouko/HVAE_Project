package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.EvaluationDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/history")
@SuppressWarnings("serial")
public class HistoryServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        try (Connection c = Db.connection()) {
            req.setAttribute("items", EvaluationDao.recent(c, 50));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        render(req, resp, "history", "Evaluation history", "history");
    }
}
