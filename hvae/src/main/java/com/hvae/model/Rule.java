package com.hvae.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * One explicit, documented evaluation rule.
 *
 * A rule is triggered when EVERY regex group matches the response. The first group is the "anchor";
 * when proximity > 0 the remaining groups must match within that many characters of the anchor.
 * If the rule is negation sensitive, a trigger is suppressed when a negation/warning cue appears
 * shortly before the anchor in the same clause.
 */
public final class Rule {
    private final String id;
    private final Value value;
    private final String name;
    private final int weight;
    private final boolean negationSensitive;
    private final boolean scenarioDriven;
    private final int proximity;
    private final String explanation;
    private final String recommendation;
    private final List<String> regexes;
    private final List<Pattern> groups;

    private Rule(Builder b) {
        this.id = b.id;
        this.value = b.value;
        this.name = b.name;
        this.weight = b.weight;
        this.negationSensitive = b.negationSensitive;
        this.scenarioDriven = b.scenarioDriven;
        this.proximity = b.proximity;
        this.explanation = b.explanation;
        this.recommendation = b.recommendation;
        this.regexes = Collections.unmodifiableList(new ArrayList<>(b.regexes));
        List<Pattern> ps = new ArrayList<>();
        for (String rx : b.regexes) ps.add(Pattern.compile(rx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        this.groups = Collections.unmodifiableList(ps);
    }

    public String getId() { return id; }
    public Value getValue() { return value; }
    public String getName() { return name; }
    public int getWeight() { return weight; }
    public boolean isNegationSensitive() { return negationSensitive; }
    public boolean isScenarioDriven() { return scenarioDriven; }
    public int getProximity() { return proximity; }
    public String getExplanation() { return explanation; }
    public String getRecommendation() { return recommendation; }
    public List<Pattern> getGroups() { return groups; }

    /** Human-readable condition, used on the rule catalogue page and stored in the database. */
    public String getConditionText() {
        if (scenarioDriven) return "Response matches a contradiction pattern defined for the selected scenario.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < regexes.size(); i++) {
            if (i > 0) sb.append("  AND  ");
            sb.append("/").append(regexes.get(i)).append("/");
        }
        if (proximity > 0 && regexes.size() > 1) sb.append("  (within ").append(proximity).append(" characters)");
        return sb.toString();
    }

    public static Builder of(String id, Value value, int weight, String name) {
        return new Builder(id, value, weight, name);
    }

    public static final class Builder {
        private final String id;
        private final Value value;
        private final int weight;
        private final String name;
        private boolean negationSensitive = true;
        private boolean scenarioDriven = false;
        private int proximity = 0;
        private String explanation = "";
        private String recommendation = "";
        private final List<String> regexes = new ArrayList<>();

        private Builder(String id, Value value, int weight, String name) {
            this.id = id; this.value = value; this.weight = weight; this.name = name;
        }
        public Builder all(String... rx) { regexes.addAll(List.of(rx)); return this; }
        public Builder near(int chars) { this.proximity = chars; return this; }
        public Builder ignoreNegation() { this.negationSensitive = false; return this; }
        public Builder scenarioDriven() { this.scenarioDriven = true; return this; }
        public Builder why(String explanation) { this.explanation = explanation; return this; }
        public Builder fix(String recommendation) { this.recommendation = recommendation; return this; }
        public Rule build() {
            if (!scenarioDriven && regexes.isEmpty()) throw new IllegalStateException("Rule " + id + " has no condition");
            return new Rule(this);
        }
    }
}
