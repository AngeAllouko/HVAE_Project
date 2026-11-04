package com.hvae.db;

import com.hvae.engine.RuleCatalogue;
import com.hvae.model.EvaluationResult;
import com.hvae.model.Outcome;
import com.hvae.model.Rule;
import com.hvae.model.Scenario;
import com.hvae.model.TriggeredRule;
import com.hvae.model.Value;
import com.hvae.model.ValueAssessment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EvaluationDao {
    private EvaluationDao() { }

    public record Summary(int id, String scenarioTitle, String overall, Timestamp createdAt, String responseExcerpt) { }

    /** Saves an evaluation with its value assessments and triggered rules in one transaction. */
    public static int save(Connection c, int scenarioId, EvaluationResult r) throws SQLException {
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int evalId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO evaluations (scenario_id, response_text, overall_outcome) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, scenarioId);
                ps.setString(2, r.getResponse());
                ps.setString(3, r.getOverall().name());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); evalId = rs.getInt(1); }
            }
            for (ValueAssessment a : r.getAssessmentList()) {
                int assessId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO value_assessments (evaluation_id, value_name, score, outcome, note) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, evalId);
                    ps.setString(2, a.getValue().name());
                    ps.setInt(3, a.getScore());
                    ps.setString(4, a.getOutcome().name());
                    ps.setString(5, a.getNote());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); assessId = rs.getInt(1); }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO triggered_rules (assessment_id, rule_id, matched_text, suppressed, suppressed_by) VALUES (?,?,?,?,?)")) {
                    for (TriggeredRule t : a.getTriggered()) {
                        ps.setInt(1, assessId);
                        ps.setString(2, t.getRule().getId());
                        ps.setString(3, t.getMatchedText());
                        ps.setBoolean(4, t.isSuppressed());
                        ps.setString(5, t.getSuppressedBy());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            c.commit();
            r.setId(evalId);
            return evalId;
        } catch (SQLException | RuntimeException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    public static List<Summary> recent(Connection c, int limit) throws SQLException {
        String sql = "SELECT e.evaluation_id, s.title, e.overall_outcome, e.created_at, e.response_text "
            + "FROM evaluations e JOIN scenarios s ON s.scenario_id = e.scenario_id ORDER BY e.evaluation_id DESC LIMIT ?";
        List<Summary> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString(5);
                    String excerpt = text.length() > 90 ? text.substring(0, 87) + "..." : text;
                    out.add(new Summary(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4), excerpt));
                }
            }
        }
        return out;
    }

    /** Re-creates a stored evaluation (rule details are looked up from the catalogue by id). */
    public static EvaluationResult load(Connection c, int id) throws SQLException {
        int scenarioId; String response; String overall; Timestamp created;
        try (PreparedStatement ps = c.prepareStatement("SELECT scenario_id, response_text, overall_outcome, created_at FROM evaluations WHERE evaluation_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                scenarioId = rs.getInt(1); response = rs.getString(2); overall = rs.getString(3); created = rs.getTimestamp(4);
            }
        }
        Scenario sc = ScenarioDao.find(c, scenarioId);
        Map<Value, ValueAssessment> map = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT assessment_id, value_name, score, outcome, note FROM value_assessments WHERE evaluation_id = ? ORDER BY assessment_id")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int aid = rs.getInt(1);
                    List<TriggeredRule> trig = new ArrayList<>();
                    try (PreparedStatement p2 = c.prepareStatement(
                            "SELECT rule_id, matched_text, suppressed, suppressed_by FROM triggered_rules WHERE assessment_id = ? ORDER BY id")) {
                        p2.setInt(1, aid);
                        try (ResultSet r2 = p2.executeQuery()) {
                            while (r2.next()) {
                                Rule rule = RuleCatalogue.byId(r2.getString(1));
                                if (rule != null) trig.add(new TriggeredRule(rule, r2.getString(2), r2.getBoolean(3), r2.getString(4)));
                            }
                        }
                    }
                    Value v = Value.valueOf(rs.getString(2));
                    map.put(v, new ValueAssessment(v, rs.getInt(3), Outcome.valueOf(rs.getString(4)), rs.getString(5), trig));
                }
            }
        }
        EvaluationResult res = new EvaluationResult(sc, response, map, Outcome.valueOf(overall));
        res.setId(id);
        res.setCreatedAt(created);
        return res;
    }
}
