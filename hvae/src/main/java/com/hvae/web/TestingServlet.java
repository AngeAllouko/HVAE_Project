package com.hvae.web;

import com.hvae.data.DatasetLoader;
import com.hvae.db.Db;
import com.hvae.db.ScenarioDao;
import com.hvae.db.TestCaseDao;
import com.hvae.engine.RuleEngine;
import com.hvae.eval.CaseResult;
import com.hvae.eval.EvaluationRunner;
import com.hvae.eval.Report;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Runs the labelled test cases and shows confusion matrices, metrics and misclassified cases. */
@WebServlet("/testing")
@SuppressWarnings("serial")
public class TestingServlet extends BaseServlet {
    private final transient RuleEngine engine = new RuleEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        try (Connection c = Db.connection()) {
            req.setAttribute("runs", TestCaseDao.listRuns(c));
            req.setAttribute("splitCounts", TestCaseDao.countBySplit(c));
            Integer runId = parseInt(req.getParameter("run"));
            if (runId != null) {
                TestCaseDao.RunSummary run = TestCaseDao.findRun(c, runId);
                if (run != null) {
                    req.setAttribute("run", run);
                    req.setAttribute("report", new Report(TestCaseDao.loadRun(c, runId)));
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        render(req, resp, "testing", "Testing and results", "testing");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;
        String split = req.getParameter("split");
        if (!"DEV".equals(split) && !"TEST".equals(split) && !"ALL".equals(split)) split = "TEST";
        try (Connection c = Db.connection()) {
            List<TestCase> cases = TestCaseDao.listAll(c);
            Map<String, Scenario> byKey = DatasetLoader.byKey(ScenarioDao.listPredefined(c));
            List<CaseResult> results = EvaluationRunner.run(engine, cases, byKey, split);
            int runId = TestCaseDao.saveRun(c, split, engine.getTau(), results);
            resp.sendRedirect(req.getContextPath() + "/testing?run=" + runId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
