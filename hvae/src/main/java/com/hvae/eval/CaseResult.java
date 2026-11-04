package com.hvae.eval;

import com.hvae.model.Outcome;
import com.hvae.model.Value;

/** Predicted vs reference outcome for one (case, value) pair. */
public final class CaseResult {
    private final String caseId;
    private final String split;
    private final Value value;
    private final Outcome reference;
    private final Outcome predicted;
    private final int score;
    private final String ruleIds;
    private final String response;

    public CaseResult(String caseId, String split, Value value, Outcome reference, Outcome predicted,
                      int score, String ruleIds, String response) {
        this.caseId = caseId; this.split = split; this.value = value; this.reference = reference;
        this.predicted = predicted; this.score = score; this.ruleIds = ruleIds; this.response = response;
    }
    public String getCaseId() { return caseId; }
    public String getSplit() { return split; }
    public Value getValue() { return value; }
    public Outcome getReference() { return reference; }
    public Outcome getPredicted() { return predicted; }
    public int getScore() { return score; }
    public String getRuleIds() { return ruleIds; }
    public String getResponse() { return response; }

    public boolean isTruePositive() { return reference.isViolation() && predicted.isViolation(); }
    public boolean isFalsePositive() { return !reference.isViolation() && predicted.isViolation(); }
    public boolean isFalseNegative() { return reference.isViolation() && !predicted.isViolation(); }
    public boolean isTrueNegative() { return !reference.isViolation() && !predicted.isViolation(); }
    /** True when the three-class outcome differs (e.g. PARTIAL predicted for MISALIGNED), even if both are flagged. */
    public boolean isExact() { return reference == predicted; }
    public String getErrorType() {
        if (isFalsePositive()) return "FP";
        if (isFalseNegative()) return "FN";
        return "";
    }
}
