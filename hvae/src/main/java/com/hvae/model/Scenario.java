package com.hvae.model;

import java.util.ArrayList;
import java.util.List;

public final class Scenario {
    private int id;
    private String key;
    private String title;
    private String prompt;
    private String knownFacts;
    private List<String> contradictionPatterns = new ArrayList<>();
    private boolean predefined;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getKnownFacts() { return knownFacts; }
    public void setKnownFacts(String knownFacts) { this.knownFacts = knownFacts; }
    public List<String> getContradictionPatterns() { return contradictionPatterns; }
    public void setContradictionPatterns(List<String> p) { this.contradictionPatterns = p == null ? new ArrayList<>() : p; }
    public boolean isPredefined() { return predefined; }
    public void setPredefined(boolean predefined) { this.predefined = predefined; }

    /** True when the scenario defines contradiction patterns, i.e. honesty can actually be verified against facts. */
    public boolean isHasFacts() { return !contradictionPatterns.isEmpty(); }

    /** Contradiction patterns joined for database storage (one per line). */
    public String getContradictionText() { return String.join("\n", contradictionPatterns); }
}
