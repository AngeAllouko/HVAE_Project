package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.TestCaseDao;
import com.hvae.eval.CaseResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Downloads one test run as CSV (for the confusion matrices and error analysis in the paper). */
@WebServlet("/export")
@SuppressWarnings("serial")
public class ExportServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        Integer runId = parseInt(req.getParameter("run"));
        if (runId == null) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing run id"); return; }
        List<CaseResult> rows;
        try (Connection c = Db.connection()) {
            rows = TestCaseDao.loadRun(c, runId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"hvae_run_" + runId + ".csv\"");
        PrintWriter w = resp.getWriter();
        w.write('\uFEFF'); // BOM so Excel reads UTF-8
        w.println("case_id,split,value,reference,predicted,error_type,score,rule_ids,response");
        for (CaseResult r : rows) {
            w.println(String.join(",", q(r.getCaseId()), q(r.getSplit()), q(r.getValue().name()), q(r.getReference().name()),
                q(r.getPredicted().name()), q(r.getErrorType()), String.valueOf(r.getScore()), q(r.getRuleIds()), q(r.getResponse())));
        }
    }

    private static String q(String s) {
        String t = s == null ? "" : s;
        // Neutralise spreadsheet formula injection, then quote.
        if (!t.isEmpty() && "=+-@".indexOf(t.charAt(0)) >= 0) t = "'" + t;
        return "\"" + t.replace("\"", "\"\"") + "\"";
    }
}
