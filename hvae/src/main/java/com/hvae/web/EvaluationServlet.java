package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.EvaluationDao;
import com.hvae.model.EvaluationResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/evaluation")
@SuppressWarnings("serial")
public class EvaluationServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        Integer id = parseInt(req.getParameter("id"));
        if (id == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing evaluation id"); return; }
        try (Connection c = Db.connection()) {
            EvaluationResult r = EvaluationDao.load(c, id);
            if (r == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Evaluation not found"); return; }
            req.setAttribute("result", r);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        render(req, resp, "result", "Evaluation #" + id, "");
    }
}
