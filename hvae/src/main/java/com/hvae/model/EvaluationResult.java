package com.hvae.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EvaluationResult {
    private Integer id;
    private final Scenario scenario;
    private final String response;
    private final Map<Value, ValueAssessment> assessments;
    private final Outcome overall;
    private Timestamp createdAt;

    public EvaluationResult(Scenario scenario, String response, Map<Value, ValueAssessment> assessments, Outcome overall) {
        this.scenario = scenario;
        this.response = response;
        this.assessments = assessments;
        this.overall = overall;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Scenario getScenario() { return scenario; }
    public String getResponse() { return response; }
    public Map<Value, ValueAssessment> getAssessments() { return assessments; }
    public List<ValueAssessment> getAssessmentList() { return new ArrayList<>(assessments.values()); }
    public Outcome getOverall() { return overall; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
