package com.hvae.eval;

import com.hvae.model.Value;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Per-value confusion matrices, macro averages and the list of misclassified cases. */
public final class Report {
    private final Map<Value, Confusion> perValue = new EnumMap<>(Value.class);
    private final Confusion overall = new Confusion();
    private final List<CaseResult> results;

    public Report(List<CaseResult> results) {
        this.results = results;
        for (Value v : Value.values()) perValue.put(v, new Confusion());
        for (CaseResult r : results) {
            perValue.get(r.getValue()).add(r);
            overall.add(r);
        }
    }

    public Map<Value, Confusion> getPerValue() { return perValue; }
    public List<Value> getValues() { return new ArrayList<>(perValue.keySet()); }
    public Confusion getOverall() { return overall; }
    public List<CaseResult> getResults() { return results; }

    public List<CaseResult> getErrors() {
        List<CaseResult> e = new ArrayList<>();
        for (CaseResult r : results) if (!r.getErrorType().isEmpty()) e.add(r);
        return e;
    }

    /** Number of three-class mismatches (includes P-vs-M disagreements that are both flagged). */
    public int getExactMatches() {
        int n = 0;
        for (CaseResult r : results) if (r.isExact()) n++;
        return n;
    }

    public String getExactMatchText() {
        return results.isEmpty() ? "n/a" : Confusion.fmt(getExactMatches() / (double) results.size());
    }

    public String getMacroAccuracyText()  { return fmt(macro(0)); }
    public String getMacroPrecisionText() { return fmt(macro(1)); }
    public String getMacroRecallText()    { return fmt(macro(2)); }
    public String getMacroF1Text()        { return fmt(macro(3)); }

    /** Macro average over values for which the metric is defined. */
    private double macro(int which) {
        double sum = 0; int n = 0;
        for (Confusion c : perValue.values()) {
            double x = which == 0 ? c.accuracy() : which == 1 ? c.precision() : which == 2 ? c.recall() : c.f1();
            if (!Double.isNaN(x)) { sum += x; n++; }
        }
        return n == 0 ? Double.NaN : sum / n;
    }

    private static String fmt(double d) { return Confusion.fmt(d); }
}
