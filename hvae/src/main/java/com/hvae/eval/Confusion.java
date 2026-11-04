package com.hvae.eval;

import java.util.Locale;

/** Binary confusion matrix where "positive" means a flagged violation (PARTIALLY_ALIGNED or MISALIGNED). */
public final class Confusion {
    private int tp, fp, tn, fn;
    private int predictedInsufficient, referenceInsufficient;

    void add(CaseResult r) {
        if (r.isTruePositive()) tp++;
        else if (r.isFalsePositive()) fp++;
        else if (r.isFalseNegative()) fn++;
        else tn++;
        if (r.getPredicted() == com.hvae.model.Outcome.INSUFFICIENT_INFORMATION) predictedInsufficient++;
        if (r.getReference() == com.hvae.model.Outcome.INSUFFICIENT_INFORMATION) referenceInsufficient++;
    }

    public int getTp() { return tp; }
    public int getFp() { return fp; }
    public int getTn() { return tn; }
    public int getFn() { return fn; }
    public int getTotal() { return tp + fp + tn + fn; }
    public int getPredictedInsufficient() { return predictedInsufficient; }
    public int getReferenceInsufficient() { return referenceInsufficient; }

    /** Each metric returns NaN when its denominator is zero (undefined). */
    public double accuracy()  { return getTotal() == 0 ? Double.NaN : (tp + tn) / (double) getTotal(); }
    public double precision() { return (tp + fp) == 0 ? Double.NaN : tp / (double) (tp + fp); }
    public double recall()    { return (tp + fn) == 0 ? Double.NaN : tp / (double) (tp + fn); }
    public double f1() {
        double p = precision(), r = recall();
        if (Double.isNaN(p) || Double.isNaN(r) || (p + r) == 0) return Double.NaN;
        return 2 * p * r / (p + r);
    }

    public String getAccuracyText()  { return fmt(accuracy()); }
    public String getPrecisionText() { return fmt(precision()); }
    public String getRecallText()    { return fmt(recall()); }
    public String getF1Text()        { return fmt(f1()); }

    public static String fmt(double d) { return Double.isNaN(d) ? "n/a" : String.format(Locale.ROOT, "%.3f", d); }
}
