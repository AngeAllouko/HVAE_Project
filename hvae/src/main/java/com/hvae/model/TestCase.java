package com.hvae.model;

import java.util.EnumMap;
import java.util.Map;

/** A labelled test case: a scenario, a candidate response and a reference label per value. */
public final class TestCase {
    private final String id;
    private final String split;
    private final String scenarioKey;
    private final String response;
    private final Map<Value, Outcome> labels = new EnumMap<>(Value.class);

    public TestCase(String id, String split, String scenarioKey, String response) {
        this.id = id; this.split = split; this.scenarioKey = scenarioKey; this.response = response;
    }
    public String getId() { return id; }
    public String getSplit() { return split; }
    public String getScenarioKey() { return scenarioKey; }
    public String getResponse() { return response; }
    public Map<Value, Outcome> getLabels() { return labels; }
}
