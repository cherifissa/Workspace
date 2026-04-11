package com.project;

import com.project.proto.FilterRequestOuterClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReportService {

    private ReportService() {
    }

    public static void afficherMoyenneJava() throws Exception {
        try (Connection conn = H2Database.createAndInitConnection()) {
            registerJavaAverage(conn);
            executeAndPrint(conn, false, null);
        }
    }

    public static void afficherMoyenneEtEcartTypeJava() throws Exception {
        try (Connection conn = H2Database.createAndInitConnection()) {
            registerJavaAndPython(conn);
            executeAndPrint(conn, true, null);
        }
    }

    public static void afficherMoyenneEtEcartType() throws Exception {
        try (Connection conn = H2Database.createAndInitConnection()) {
            registerNative(conn);
            executeAndPrint(conn, true, null);
        }
    }

    public static void afficherMoyenneEtEcartType2(FilterRequestOuterClass.FilterRequest request) throws Exception {
        try (Connection conn = H2Database.createAndInitConnection()) {
            registerNative(conn);
            executeAndPrint(conn, true, request);
        }
    }

    private static void registerJavaAverage(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP AGGREGATE IF EXISTS MOYENNE");
            stmt.execute("CREATE AGGREGATE MOYENNE FOR \"com.project.udf.MoyenneAggregate\"");
        }
    }

    private static void registerJavaAndPython(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP AGGREGATE IF EXISTS MOYENNE");
            stmt.execute("DROP AGGREGATE IF EXISTS ECARTTYPE");
            stmt.execute("CREATE AGGREGATE MOYENNE FOR \"com.project.udf.MoyenneAggregate\"");
            stmt.execute("CREATE AGGREGATE ECARTTYPE FOR \"com.project.udf.EcartTypePythonAggregate\"");
        }
    }

    private static void registerNative(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP AGGREGATE IF EXISTS MOYENNE");
            stmt.execute("DROP AGGREGATE IF EXISTS ECARTTYPE");
            stmt.execute("CREATE AGGREGATE MOYENNE FOR \"com.project.udf.NativeMoyenneAggregate\"");
            stmt.execute("CREATE AGGREGATE ECARTTYPE FOR \"com.project.udf.NativeEcartTypeAggregate\"");
        }
    }

    private static void executeAndPrint(Connection conn, boolean includeStdDev, FilterRequestOuterClass.FilterRequest filter) throws Exception {
        List<List<String>> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH order_totals AS (\n");
        sql.append("  SELECT\n");
        sql.append("    o.\"Customer ID\" AS customer_id,\n");
        sql.append("    o.\"Order ID\" AS order_id,\n");
        sql.append("    SUM(\n");
        sql.append("      CAST(od.\"QUANTITY\" AS DOUBLE)\n");
        sql.append("      * CAST(REPLACE(od.\"Unit Price\", ',', '.') AS DOUBLE)\n");
        sql.append("      * (1.0 - CAST(REPLACE(COALESCE(od.\"DISCOUNT\", '0'), ',', '.') AS DOUBLE))\n");
        sql.append("    ) AS order_total\n");
        sql.append("  FROM \"ORDER\" o\n");
        sql.append("  JOIN ORDERDETAILS od ON od.\"Order ID\" = o.\"Order ID\"\n");
        sql.append("  GROUP BY o.\"Customer ID\", o.\"Order ID\"\n");
        sql.append(")\n");
        sql.append("SELECT\n");
        sql.append("  c.\"First Name\" AS client,\n");
        sql.append("  MOYENNE(ot.order_total) AS moyenne");
        if (includeStdDev) {
            sql.append(",\n  ECARTTYPE(ot.order_total) AS ecarttype\n");
        } else {
            sql.append("\n");
        }
        sql.append("FROM CUSTOMER c\n");
        sql.append("JOIN order_totals ot ON ot.customer_id = c.ID\n");

        List<Object> params = new ArrayList<>();
        if (filter != null && filter.getCustomersCount() > 0) {
            sql.append("WHERE c.\"First Name\" IN (");
            for (int i = 0; i < filter.getCustomersCount(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("?");
                params.add(filter.getCustomers(i));
            }
            sql.append(")\n");
        }

        sql.append("GROUP BY c.\"First Name\"\n");

        if (filter != null) {
            sql.append("HAVING MOYENNE(ot.order_total) >= ?\n");
            params.add(filter.getMinTotal());
        }

        sql.append("ORDER BY c.\"First Name\"");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (p instanceof Number n) {
                    ps.setDouble(i + 1, n.doubleValue());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    row.add(rs.getString("client"));
                    row.add(formatDouble(rs.getDouble("moyenne")));
                    if (includeStdDev) {
                        row.add(formatDouble(rs.getDouble("ecarttype")));
                    }
                    rows.add(row);
                }
            }
        }

        List<String> headers = new ArrayList<>();
        headers.add("CLIENT");
        headers.add("MOYENNE");
        if (includeStdDev) {
            headers.add("ECARTTYPE");
        }

        TablePrinter.print(headers, rows);
    }

    private static String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
