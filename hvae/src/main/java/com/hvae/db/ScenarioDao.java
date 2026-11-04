package com.hvae.db;

import com.hvae.model.Scenario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class ScenarioDao {
    private ScenarioDao() { }

    private static final String COLS = "scenario_id, scenario_key, title, user_prompt, known_facts, contradiction_patterns, is_predefined";

    public static List<Scenario> listPredefined(Connection c) throws SQLException {
        List<Scenario> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM scenarios WHERE is_predefined = TRUE ORDER BY scenario_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public static Scenario find(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM scenarios WHERE scenario_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public static int countPredefined(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM scenarios WHERE is_predefined = TRUE")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static int insert(Connection c, Scenario s) throws SQLException {
        String sql = "INSERT INTO scenarios (scenario_key, title, user_prompt, known_facts, contradiction_patterns, is_predefined) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getKey());
            ps.setString(2, s.getTitle());
            ps.setString(3, s.getPrompt());
            ps.setString(4, s.getKnownFacts());
            ps.setString(5, s.getContradictionText().isEmpty() ? null : s.getContradictionText());
            ps.setBoolean(6, s.isPredefined());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                s.setId(rs.getInt(1));
                return s.getId();
            }
        }
    }

    private static Scenario map(ResultSet rs) throws SQLException {
        Scenario s = new Scenario();
        s.setId(rs.getInt("scenario_id"));
        s.setKey(rs.getString("scenario_key"));
        s.setTitle(rs.getString("title"));
        s.setPrompt(rs.getString("user_prompt"));
        s.setKnownFacts(rs.getString("known_facts"));
        String cp = rs.getString("contradiction_patterns");
        List<String> pats = new ArrayList<>();
        if (cp != null) for (String p : cp.split("\n")) if (!p.isBlank()) pats.add(p);
        s.setContradictionPatterns(pats);
        s.setPredefined(rs.getBoolean("is_predefined"));
        return s;
    }
}
