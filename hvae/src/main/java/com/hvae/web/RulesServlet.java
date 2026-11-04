package com.hvae.web;

import com.hvae.engine.RuleCatalogue;
import com.hvae.engine.RuleEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Shows the documented rule catalogue (does not need the database). */
@WebServlet("/rules")
@SuppressWarnings("serial")
public class RulesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("rules", RuleCatalogue.all());
        req.setAttribute("tau", RuleEngine.DEFAULT_TAU);
        req.setAttribute("window", RuleEngine.DEFAULT_WINDOW);
        req.setAttribute("pageTitle", "Rule catalogue");
        req.setAttribute("nav", "rules");
        req.getRequestDispatcher("/WEB-INF/views/rules.jsp").forward(req, resp);
    }
}
