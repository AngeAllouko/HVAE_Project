package com.hvae.model;

/** A rule that matched. If suppressed, a negation/warning cue neutralised it (kept for transparency). */
public final class TriggeredRule {
    private final Rule rule;
    private final String matchedText;
    private final boolean suppressed;
    private final String suppressedBy;

    public TriggeredRule(Rule rule, String matchedText, boolean suppressed, String suppressedBy) {
        this.rule = rule;
        this.matchedText = matchedText;
        this.suppressed = suppressed;
        this.suppressedBy = suppressedBy;
    }

    public Rule getRule() { return rule; }
    public String getMatchedText() { return matchedText; }
    public boolean isSuppressed() { return suppressed; }
    public String getSuppressedBy() { return suppressedBy; }
}
