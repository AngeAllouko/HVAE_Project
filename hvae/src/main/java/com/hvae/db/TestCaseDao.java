package com.hvae.db;

import com.hvae.eval.CaseResult;
import com.hvae.model.Outcome;
import com.hvae.model.TestCase;
import com.hvae.model.Value;

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

public final class TestCaseDao {
    private TestCaseDao() { }

    public record RunSummary(int id, Timestamp runAt, String split, int tau, int nCases) { }

    public static int count(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM test_cases")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Counts test cases per split, e.g. {DEV=30, TEST=45}. */
    public static Map<String, Integer> countBySplit(Connection c) throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT split_name, COUNT(*) FROM test_cases GROUP BY split_name ORDER BY split_name")) {
            while (rs.next()) m.put(rs.getString(1), rs.getInt(2));
        }
        return m;
    }

    public static void insert(Connection c, TestCase tc, int scenarioId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO test_cases (case_id, scenario_id, split_name, response_text) VALUES (?,?,?,?)")) {
            ps.setString(1, tc.getId());
            ps.setInt(2, scenarioId);
            ps.setString(3, tc.getSplit());
            ps.setString(4, tc.getResponse());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO test_case_labels (case_id, value_name, reference_label) VALUES (?,?,?)")) {
            for (Map.Entry<Value, Outcome> e : tc.getLabels().entrySet()) {
                ps.setString(1, tc.getId());
                ps.setString(2, e.getKey().name());
                ps.setString(3, e.getValue().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static List<TestCase> listAll(Connection c) throws SQLException {
        Map<String, TestCase> map = new LinkedHashMap<>();
        String sql = "SELECT t.case_id, t.split_name, s.scenario_key, t.response_text FROM test_cases t "
            + "JOIN scenarios s ON s.scenario_id = t.scenario_id ORDER BY t.case_id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString(1), new TestCase(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        }
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT case_id, value_name, reference_label FROM test_case_labels")) {
            while (rs.next()) {
                TestCase tc = map.get(rs.getString(1));
                if (tc != null) tc.getLabels().put(Value.valueOf(rs.getString(2)), Outcome.valueOf(rs.getString(3)));
            }
        }
        return new ArrayList<>(map.values());
    }

    public static int saveRun(Connection c, String split, int tau, List<CaseResult> results) throws SQLException {
        boolean auto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int runId;
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO test_runs (split_name, tau, n_cases) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, split);
                ps.setInt(2, tau);
                ps.setInt(3, results.size() / Value.values().length);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); runId = rs.getInt(1); }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO test_run_results (run_id, case_id, value_name, predicted, reference, score, rule_ids) VALUES (?,?,?,?,?,?,?)")) {
                for (CaseResult r : results) {
                    ps.setInt(1, runId);
                    ps.setString(2, r.getCaseId());
                    ps.setString(3, r.getValue().name());
                    ps.setString(4, r.getPredicted().name());
                    ps.setString(5, r.getReference().name());
                    ps.setInt(6, r.getScore());
                    ps.setString(7, r.getRuleIds());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            return runId;
        } catch (SQLException | RuntimeException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(auto);
        }
    }

    public static List<RunSummary> listRuns(Connection c) throws SQLException {
        List<RunSummary> out = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT run_id, run_at, split_name, tau, n_cases FROM test_runs ORDER BY run_id DESC LIMIT 20")) {
            while (rs.next()) out.add(new RunSummary(rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getInt(4), rs.getInt(5)));
        }
        return out;
    }

    public static RunSummary findRun(Connection c, int runId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT run_id, run_at, split_name, tau, n_cases FROM test_runs WHERE run_id = ?")) {
            ps.setInt(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new RunSummary(rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getInt(4), rs.getInt(5)) : null;
            }
        }
    }

    public static List<CaseResult> loadRun(Connection c, int runId) throws SQLException {
        String sql = "SELECT r.case_id, t.split_name, r.value_name, r.reference, r.predicted, r.score, r.rule_ids, t.response_text "
            + "FROM test_run_results r JOIN test_cases t ON t.case_id = r.case_id WHERE r.run_id = ? ORDER BY r.case_id, r.value_name";
        List<CaseResult> out = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new CaseResult(rs.getString(1), rs.getString(2), Value.valueOf(rs.getString(3)),
                        Outcome.valueOf(rs.getString(4)), Outcome.valueOf(rs.getString(5)), rs.getInt(6),
                        rs.getString(7) == null ? "" : rs.getString(7), rs.getString(8)));
                }
            }
        }
        return out;
    }
}
