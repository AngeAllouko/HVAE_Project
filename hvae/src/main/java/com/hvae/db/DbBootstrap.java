package com.hvae.db;

import com.hvae.data.DatasetLoader;
import com.hvae.engine.RuleCatalogue;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Creates tables, mirrors the rule catalogue, and loads the bundled scenarios and test cases on first run. */
public final class DbBootstrap {
    private static final Logger LOG = Logger.getLogger(DbBootstrap.class.getName());
    private DbBootstrap() { }

    public static synchronized boolean run() {
        try (Connection c = Db.connection()) {
            Db.createSchema(c);
            RuleDao.upsertAll(c, RuleCatalogue.all());
            if (ScenarioDao.countPredefined(c) == 0) {
                try (InputStream in = DbBootstrap.class.getResourceAsStream("/scenarios.tsv")) {
                    for (Scenario s : DatasetLoader.loadScenarios(in)) ScenarioDao.insert(c, s);
                }
            }
            if (TestCaseDao.count(c) == 0) {
                Map<String, Scenario> byKey;
                List<TestCase> cases;
                byKey = DatasetLoader.byKey(ScenarioDao.listPredefined(c));
                try (InputStream in = DbBootstrap.class.getResourceAsStream("/test_cases.tsv")) {
                    cases = DatasetLoader.loadTestCases(in);
                }
                for (TestCase tc : cases) TestCaseDao.insert(c, tc, byKey.get(tc.getScenarioKey()).getId());
            }
            Db.markReady();
            LOG.info("HVAE database ready.");
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "HVAE database initialisation failed", e);
            Db.markFailed(e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }
}
