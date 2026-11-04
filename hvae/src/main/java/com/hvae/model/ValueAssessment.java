package com.hvae.model;

import java.util.ArrayList;
import java.util.List;

public final class ValueAssessment {
    private final Value value;
    private final int score;
    private final Outcome outcome;
    private final String note;
    private final List<TriggeredRule> triggered;

    public ValueAssessment(Value value, int score, Outcome outcome, String note, List<TriggeredRule> triggered) {
        this.value = value;
        this.score = score;
        this.outcome = outcome;
        this.note = note;
        this.triggered = triggered;
    }

    public Value getValue() { return value; }
    public int getScore() { return score; }
    public Outcome getOutcome() { return outcome; }
    public String getNote() { return note; }
    public List<TriggeredRule> getTriggered() { return triggered; }

    public List<TriggeredRule> getActive() {
        List<TriggeredRule> out = new ArrayList<>();
        for (TriggeredRule t : triggered) if (!t.isSuppressed()) out.add(t);
        return out;
    }

    public List<String> getActiveRuleIds() {
        List<String> ids = new ArrayList<>();
        for (TriggeredRule t : getActive()) ids.add(t.getRule().getId());
        return ids;
    }
}
