package com.hvae.cli;

import com.hvae.data.DatasetLoader;
import com.hvae.engine.RuleEngine;
import com.hvae.eval.CaseResult;
import com.hvae.eval.Confusion;
import com.hvae.eval.EvaluationRunner;
import com.hvae.eval.Report;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;
import com.hvae.model.Value;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** Command-line runner (no database needed): java com.hvae.cli.EvaluateDataset [DEV|TEST|ALL] */
public final class EvaluateDataset {
    public static void main(String[] args) throws Exception {
        String split = args.length > 0 ? args[0].toUpperCase() : "TEST";
        List<Scenario> scenarios;
        List<TestCase> cases;
        try (InputStream a = EvaluateDataset.class.getResourceAsStream("/scenarios.tsv");
             InputStream b = EvaluateDataset.class.getResourceAsStream("/test_cases.tsv")) {
            scenarios = DatasetLoader.loadScenarios(a);
            cases = DatasetLoader.loadTestCases(b);
        }
        Map<String, Scenario> byKey = DatasetLoader.byKey(scenarios);
        Report rep = new Report(EvaluationRunner.run(new RuleEngine(), cases, byKey, split));
        System.out.println("Split: " + split + "   (positive = PARTIALLY_ALIGNED or MISALIGNED)");
        System.out.printf("%-10s %4s %4s %4s %4s  %6s %6s %6s %6s%n", "Value", "TP", "FP", "TN", "FN", "Acc", "Prec", "Rec", "F1");
        for (Value v : Value.values()) {
            Confusion c = rep.getPerValue().get(v);
            System.out.printf("%-10s %4d %4d %4d %4d  %6s %6s %6s %6s%n", v.getLabel(), c.getTp(), c.getFp(), c.getTn(), c.getFn(),
                c.getAccuracyText(), c.getPrecisionText(), c.getRecallText(), c.getF1Text());
        }
        System.out.printf("%-10s %4s %4s %4s %4s  %6s %6s %6s %6s%n", "Macro", "", "", "", "",
            rep.getMacroAccuracyText(), rep.getMacroPrecisionText(), rep.getMacroRecallText(), rep.getMacroF1Text());
        System.out.println("Exact three-class match: " + rep.getExactMatchText());
        System.out.println("\nErrors:");
        for (CaseResult r : rep.getErrors()) {
            System.out.printf("  %s %s %-9s ref=%s pred=%s rules=[%s]  \"%s\"%n", r.getCaseId(), r.getErrorType(), r.getValue(),
                r.getReference().getCode(), r.getPredicted().getCode(), r.getRuleIds(), r.getResponse());
        }
    }
}
