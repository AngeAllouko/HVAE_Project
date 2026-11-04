package com.hvae.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/** JDBC access. Settings come from db.properties; environment variables override them. */
public final class Db {
    private static final Properties PROPS = new Properties();
    private static volatile boolean ready;
    private static volatile String initError = "Database has not been initialised yet.";

    static {
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ignored) { }
        registerDrivers();
    }

    /**
     * In a servlet container, JDBC drivers in WEB-INF/lib are not always registered with DriverManager
     * automatically ("No suitable driver found"). Loading them through this class's own class loader
     * makes each driver register itself.
     */
    private static void registerDrivers() {
        try {
            for (Driver d : ServiceLoader.load(Driver.class, Db.class.getClassLoader())) {
                if (d == null) continue; // instantiation is what registers the driver
            }
        } catch (ServiceConfigurationError ignored) { }
    }

    private Db() { }

    private static String setting(String key, String env) {
        String e = System.getenv(env);
        if (e != null && !e.isBlank()) return e;
        String p = PROPS.getProperty(key);
        return p == null ? "" : p;
    }

    public static Connection connection() throws SQLException {
        return DriverManager.getConnection(setting("db.url", "HVAE_DB_URL"),
            setting("db.user", "HVAE_DB_USER"), setting("db.password", "HVAE_DB_PASSWORD"));
    }

    public static boolean isReady() { return ready; }
    public static String getInitError() { return initError; }
    static void markReady() { ready = true; initError = null; }
    static void markFailed(String msg) { ready = false; initError = msg; }

    /** Executes every statement in /schema.sql (all are CREATE TABLE IF NOT EXISTS). */
    static void createSchema(Connection c) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = Db.class.getResourceAsStream("/schema.sql");
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("--")) continue;
                sb.append(line).append('\n');
            }
        }
        try (Statement st = c.createStatement()) {
            for (String sql : sb.toString().split(";\\s*\\n")) {
                if (!sql.isBlank()) st.execute(sql);
            }
        }
    }
}
