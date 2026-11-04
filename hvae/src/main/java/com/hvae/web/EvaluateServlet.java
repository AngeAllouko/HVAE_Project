package com.hvae.web;

import com.hvae.db.Db;
import com.hvae.db.EvaluationDao;
import com.hvae.db.ScenarioDao;
import com.hvae.engine.RuleEngine;
import com.hvae.model.EvaluationResult;
import com.hvae.model.Scenario;
import com.hvae.model.Value;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Validates the form, runs the rule engine, stores the evaluation, then redirects to the result page. */
@WebServlet("/evaluate")
@SuppressWarnings("serial")
public class EvaluateServlet extends BaseServlet {
    private static final int MAX_RESPONSE = 5000;
    private static final int MAX_PROMPT = 1000;
    private static final int MAX_TITLE = 150;
    private final transient RuleEngine engine = new RuleEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireDb(req, resp)) return;

        String scenarioParam = trim(req.getParameter("scenarioId"));
        String responseText = trim(req.getParameter("response"));
        String customTitle = trim(req.getParameter("customTitle"));
        String customPrompt = trim(req.getParameter("customPrompt"));

        List<String> errors = new ArrayList<>();
        Set<Value> values = EnumSet.noneOf(Value.class);
        String[] vs = req.getParameterValues("values");
        if (vs != null) {
            for (String v : vs) {
                try { values.add(Value.valueOf(v)); } catch (IllegalArgumentException ignored) { }
            }
        }
        if (values.isEmpty()) errors.add("Select at least one value to evaluate.");
        if (responseText.isEmpty()) errors.add("Enter the candidate AI response to evaluate.");
        if (responseText.length() > MAX_RESPONSE) errors.add("The response is too long (maximum " + MAX_RESPONSE + " characters).");

        boolean custom = "custom".equals(scenarioParam);
        Integer scenarioId = custom ? null : parseInt(scenarioParam);
        if (custom) {
            if (customPrompt.isEmpty()) errors.add("Describe the custom scenario (the user request the AI was responding to).");
            if (customPrompt.length() > MAX_PROMPT) errors.add("The scenario description is too long (maximum " + MAX_PROMPT + " characters).");
            if (customTitle.length() > MAX_TITLE) errors.add("The scenario title is too long (maximum " + MAX_TITLE + " characters).");
        } else if (scenarioId == null) {
            errors.add("Select a scenario.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            HomeServlet.showForm(req, resp);
            return;
        }

        try (Connection c = Db.connection()) {
            Scenario sc;
            if (custom) {
                sc = new Scenario();
                sc.setTitle(customTitle.isEmpty() ? "Custom scenario" : customTitle);
                sc.setPrompt(customPrompt);
                sc.setPredefined(false);
                ScenarioDao.insert(c, sc);
            } else {
                sc = ScenarioDao.find(c, scenarioId);
                if (sc == null) {
                    errors.add("The selected scenario does not exist.");
                    req.setAttribute("errors", errors);
                    HomeServlet.showForm(req, resp);
                    return;
                }
            }
            EvaluationResult result = engine.evaluate(sc, responseText, values);
            int id = EvaluationDao.save(c, sc.getId(), result);
            resp.sendRedirect(req.getContextPath() + "/evaluation?id=" + id);
        } catch (SQLException e) {
            throw new ServletException("Could not save the evaluation", e);
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
