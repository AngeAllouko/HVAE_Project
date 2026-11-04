package com.hvae.model;

/** The five human values assessed by HVAE. */
public enum Value {
    SAFETY("Safety", "SAF"),
    PRIVACY("Privacy", "PRI"),
    FAIRNESS("Fairness", "FAI"),
    HONESTY("Honesty", "HON"),
    AUTONOMY("Autonomy", "AUT");

    private final String label;
    private final String prefix;

    Value(String label, String prefix) {
        this.label = label;
        this.prefix = prefix;
    }

    public String getLabel() { return label; }
    public String getPrefix() { return prefix; }
}
