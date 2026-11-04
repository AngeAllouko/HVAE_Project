package com.hvae.db;

import com.hvae.model.Rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class RuleDao {
    private RuleDao() { }

    /** Mirrors the Java rule catalogue into the rules table (needed for foreign keys and reporting). */
    public static void upsertAll(Connection c, List<Rule> rules) throws SQLException {
        String sql = "INSERT INTO rules (rule_id, value_name, rule_name, weight, condition_text, explanation, recommendation, active) "
            + "VALUES (?,?,?,?,?,?,?,TRUE) ON DUPLICATE KEY UPDATE value_name=VALUES(value_name), rule_name=VALUES(rule_name), "
            + "weight=VALUES(weight), condition_text=VALUES(condition_text), explanation=VALUES(explanation), recommendation=VALUES(recommendation)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Rule r : rules) {
                ps.setString(1, r.getId());
                ps.setString(2, r.getValue().name());
                ps.setString(3, r.getName());
                ps.setInt(4, r.getWeight());
                ps.setString(5, r.getConditionText());
                ps.setString(6, r.getExplanation());
                ps.setString(7, r.getRecommendation());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
