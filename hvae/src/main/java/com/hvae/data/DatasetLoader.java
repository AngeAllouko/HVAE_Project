package com.hvae.data;

import com.hvae.model.Outcome;
import com.hvae.model.Scenario;
import com.hvae.model.TestCase;
import com.hvae.model.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads the bundled tab-separated scenario and test-case files. */
public final class DatasetLoader {
    private DatasetLoader() { }

    public static List<Scenario> loadScenarios(InputStream in) throws IOException {
        List<Scenario> out = new ArrayList<>();
        for (String[] c : rows(in, 5)) {
            Scenario s = new Scenario();
            s.setKey(c[0]);
            s.setTitle(c[1]);
            s.setPrompt(c[2]);
            s.setKnownFacts(c[3].isBlank() ? null : c[3]);
            List<String> pats = new ArrayList<>();
            if (!c[4].isBlank()) for (String p : c[4].split("\\s;;\\s")) if (!p.isBlank()) pats.add(p.trim());
            s.setContradictionPatterns(pats);
            s.setPredefined(true);
            out.add(s);
        }
        return out;
    }

    public static List<TestCase> loadTestCases(InputStream in) throws IOException {
        List<TestCase> out = new ArrayList<>();
        Value[] vals = Value.values();
        for (String[] c : rows(in, 9)) {
            TestCase t = new TestCase(c[0], c[1], c[2], c[3]);
            for (int i = 0; i < vals.length; i++) t.getLabels().put(vals[i], Outcome.parse(c[4 + i]));
            out.add(t);
        }
        return out;
    }

    public static Map<String, Scenario> byKey(List<Scenario> list) {
        Map<String, Scenario> m = new LinkedHashMap<>();
        for (Scenario s : list) m.put(s.getKey(), s);
        return m;
    }

    private static List<String[]> rows(InputStream in, int minCols) throws IOException {
        List<String[]> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] c = line.split("\t", -1);
                if (c.length < minCols) throw new IOException("Malformed line (expected " + minCols + " columns): " + line);
                out.add(c);
            }
        }
        return out;
    }
}
