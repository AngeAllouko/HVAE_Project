package com.hvae;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hvae.data.DatasetLoader;
import com.hvae.engine.RuleEngine;
import com.hvae.eval.CaseResult;
import com.hvae.eval.Confusion;
import com.hvae.eval.EvaluationRunner;
import com.hvae.eval.Report;
import com.hvae.model.Outcome;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;
import com.hvae.model.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DatasetAndMetricsTest {

    private static CaseResult cr(Value v, Outcome ref, Outcome pred) {
        return new CaseResult("X", "TEST", v, ref, pred, 0, "", "text");
    }

    @Test
    void metricsMatchHandComputation() {
        // 2 TP, 1 FP, 1 FN, 3 TN for SAFETY
        List<CaseResult> rs = new ArrayList<>();
        rs.add(cr(Value.SAFETY, Outcome.MISALIGNED, Outcome.MISALIGNED));
        rs.add(cr(Value.SAFETY, Outcome.PARTIALLY_ALIGNED, Outcome.MISALIGNED));
        rs.add(cr(Value.SAFETY, Outcome.ALIGNED, Outcome.PARTIALLY_ALIGNED));
        rs.add(cr(Value.SAFETY, Outcome.MISALIGNED, Outcome.ALIGNED));
        for (int i = 0; i < 3; i++) rs.add(cr(Value.SAFETY, Outcome.ALIGNED, Outcome.ALIGNED));
        Confusion c = new Report(rs).getPerValue().get(Value.SAFETY);
        assertEquals(2, c.getTp()); assertEquals(1, c.getFp()); assertEquals(1, c.getFn()); assertEquals(3, c.getTn());
        assertEquals(5 / 7.0, c.accuracy(), 1e-9);
        assertEquals(2 / 3.0, c.precision(), 1e-9);
        assertEquals(2 / 3.0, c.recall(), 1e-9);
        assertEquals(2 / 3.0, c.f1(), 1e-9);
    }

    @Test
    void undefinedMetricsAreReportedAsNa() {
        Report rep = new Report(List.of(cr(Value.PRIVACY, Outcome.ALIGNED, Outcome.ALIGNED)));
        Confusion c = rep.getPerValue().get(Value.PRIVACY);
        assertTrue(Double.isNaN(c.precision()));
        assertTrue(Double.isNaN(c.recall()));
        assertEquals("n/a", c.getF1Text());
        assertEquals("1.000", c.getAccuracyText());
    }

    @Test
    void insufficientInformationCountsAsNotFlagged() {
        Confusion c = new Report(List.of(cr(Value.HONESTY, Outcome.MISALIGNED, Outcome.INSUFFICIENT_INFORMATION)))
            .getPerValue().get(Value.HONESTY);
        assertEquals(1, c.getFn());
        assertEquals(1, c.getPredictedInsufficient());
    }

    @Test
    void bundledDatasetIsConsistent() throws IOException {
        List<Scenario> scenarios;
        List<TestCase> cases;
        try (InputStream a = getClass().getResourceAsStream("/scenarios.tsv");
             InputStream b = getClass().getResourceAsStream("/test_cases.tsv")) {
            scenarios = DatasetLoader.loadScenarios(a);
            cases = DatasetLoader.loadTestCases(b);
        }
        Map<String, Scenario> byKey = DatasetLoader.byKey(scenarios);
        assertEquals(9, scenarios.size());
        assertEquals(75, cases.size());
        long dev = cases.stream().filter(t -> t.getSplit().equals("DEV")).count();
        long test = cases.stream().filter(t -> t.getSplit().equals("TEST")).count();
        assertEquals(30, dev);
        assertEquals(45, test);
        for (TestCase t : cases) {
            assertTrue(byKey.containsKey(t.getScenarioKey()), "unknown scenario for " + t.getId());
            assertEquals(5, t.getLabels().size(), t.getId());
        }
        // The runner produces one result per case and value, and respects the split filter.
        assertEquals(75 * 5, EvaluationRunner.run(new RuleEngine(), cases, byKey, "ALL").size());
        assertEquals(45 * 5, EvaluationRunner.run(new RuleEngine(), cases, byKey, "TEST").size());
    }
}
