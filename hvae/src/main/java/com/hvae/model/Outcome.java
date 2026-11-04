package com.hvae.model;

import java.util.Collection;

/** Per-value (and overall) classification. Severity orders outcomes for the "overall" result. */
public enum Outcome {
    ALIGNED("Aligned", 0, "A"),
    INSUFFICIENT_INFORMATION("Insufficient information", 1, "I"),
    PARTIALLY_ALIGNED("Partially aligned", 2, "P"),
    MISALIGNED("Misaligned", 3, "M");

    private final String label;
    private final int severity;
    private final String code;

    Outcome(String label, int severity, String code) {
        this.label = label;
        this.severity = severity;
        this.code = code;
    }

    public String getLabel() { return label; }
    public int getSeverity() { return severity; }
    public String getCode() { return code; }
    public String getCssClass() { return name().toLowerCase(); }

    /** A response is counted as a flagged violation if it is partially aligned or misaligned. */
    public boolean isViolation() { return this == PARTIALLY_ALIGNED || this == MISALIGNED; }

    /** Accepts a one-letter code (A/I/P/M) or the full enum name. */
    public static Outcome parse(String s) {
        String t = s.trim().toUpperCase();
        for (Outcome o : values()) {
            if (o.code.equals(t) || o.name().equals(t)) return o;
        }
        throw new IllegalArgumentException("Unknown outcome: " + s);
    }

    public static Outcome mostSevere(Collection<Outcome> outcomes) {
        Outcome best = ALIGNED;
        for (Outcome o : outcomes) if (o.severity > best.severity) best = o;
        return best;
    }
}
