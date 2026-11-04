package com.hvae.eval;

import com.hvae.engine.RuleEngine;
import com.hvae.model.EvaluationResult;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;
import com.hvae.model.Value;
import com.hvae.model.ValueAssessment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Runs labelled test cases through the rule engine (all five values) and collects results. */
public final class EvaluationRunner {
    private EvaluationRunner() { }

    /** @param split "DEV", "TEST" or "ALL" */
    public static List<CaseResult> run(RuleEngine engine, List<TestCase> cases, Map<String, Scenario> scenarios, String split) {
        List<CaseResult> out = new ArrayList<>();
        for (TestCase tc : cases) {
            if (!"ALL".equalsIgnoreCase(split) && !tc.getSplit().equalsIgnoreCase(split)) continue;
            Scenario sc = scenarios.get(tc.getScenarioKey());
            EvaluationResult res = engine.evaluate(sc, tc.getResponse(), Arrays.asList(Value.values()));
            for (Value v : Value.values()) {
                ValueAssessment a = res.getAssessments().get(v);
                out.add(new CaseResult(tc.getId(), tc.getSplit(), v, tc.getLabels().get(v), a.getOutcome(),
                    a.getScore(), String.join(",", a.getActiveRuleIds()), tc.getResponse()));
            }
        }
        return out;
    }
}
