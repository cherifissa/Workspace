package com.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public final class H2Database {

    private H2Database() {
    }

    public static Connection createAndInitConnection() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");
        initVirtualTables(conn);
        return conn;
    }

    public static void initVirtualTables(Connection conn) throws Exception {
        String customers = resolveCsvPath("Customers.csv");
        String orders = resolveCsvPath("Orders.csv");
        String orderDetails = resolveCsvPath("OrderDetails.csv");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE OR REPLACE VIEW CUSTOMER AS SELECT * FROM CSVREAD('" + customers + "', null, 'fieldSeparator=;')");
            stmt.execute("CREATE OR REPLACE VIEW \"ORDER\" AS SELECT * FROM CSVREAD('" + orders + "', null, 'fieldSeparator=;')");
            stmt.execute("CREATE OR REPLACE VIEW ORDERDETAILS AS SELECT * FROM CSVREAD('" + orderDetails + "', null, 'fieldSeparator=;')");
        }
    }

    private static String resolveCsvPath(String fileName) {
        List<Path> candidates = List.of(
                Path.of("src/main/resources/data", fileName),
                Path.of("java-project/src/main/resources/data", fileName),
            Path.of("../java-project/src/main/resources/data", fileName),
            Path.of("../../java-project/src/main/resources/data", fileName),
                Path.of("data", fileName),
            Path.of("../data", fileName),
            Path.of("../../data", fileName)
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString().replace("\\", "/").replace("'", "''");
            }
        }

        throw new IllegalStateException("CSV not found: " + fileName);
    }
}
