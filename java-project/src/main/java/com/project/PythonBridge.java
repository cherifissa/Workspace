package com.project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PythonBridge {

    public static double ecartType(List<Double> values) throws Exception {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        Path script = resolveScriptPath();

        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                script.toString()
        );
        pb.redirectErrorStream(true);

        Process p = pb.start();

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
            for (double v : values) {
                w.write(Double.toString(v));
                w.newLine();
            }
        }

        String line;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            line = r.readLine();
        }

        int exit = p.waitFor();
        if (exit != 0 || line == null || line.isBlank()) {
            throw new IllegalStateException("Python/Numpy failed for ECARTTYPE");
        }

        return Double.parseDouble(line.trim());
    }

    private static Path resolveScriptPath() {
        List<Path> candidates = List.of(
                Path.of("src/main/python/std.py"),
                Path.of("java-project/src/main/python/std.py"),
            Path.of("../java-project/src/main/python/std.py"),
            Path.of("../../java-project/src/main/python/std.py")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to locate Python script for ECARTTYPE");
    }
}