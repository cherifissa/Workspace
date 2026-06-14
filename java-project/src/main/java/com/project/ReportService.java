package com.project;

import java.sql.*;
import java.util.*;
import java.util.Locale;


public final class ReportService {

    public static boolean USE_CPP_ECARTTYPE = false;

    private ReportService() {}

    public static void afficherMoyenneJava() throws Exception {
        try (Connection conn = Database.connect()) {
            enregistrer(conn, false);
            afficher(conn, false);
        }
    }

    public static void afficherMoyenneEtEcartType() throws Exception {
        try (Connection conn = Database.connect()) {
            enregistrer(conn, true);
            afficher(conn, true);
        }
    }

    private static void enregistrer(Connection conn, boolean avecEcart) throws Exception {
        try (Statement s = conn.createStatement()) {
            s.execute("DROP AGGREGATE IF EXISTS MOYENNE");
            s.execute("CREATE AGGREGATE MOYENNE FOR \"com.project.udf.MoyenneAggregate\"");
            if (avecEcart) {
                s.execute("DROP AGGREGATE IF EXISTS ECARTTYPE");
                String impl = USE_CPP_ECARTTYPE
                    ? "com.project.udf.NativeEcartTypeAggregate"
                    : "com.project.udf.EcartTypePythonAggregate";
                s.execute("CREATE AGGREGATE ECARTTYPE FOR \"" + impl + "\"");
            }
        }
    }

    private static void afficher(Connection conn, boolean avecEcart) throws Exception {
        String ecart = avecEcart ? ",\n  ECARTTYPE(ot.order_total) AS ecarttype" : "";
        String sql =
            "WITH order_totals AS (\n" +
            "  SELECT o.Customer_ID AS customer_id, o.Order_ID AS order_id,\n" +
            "    SUM(\n" +
            "      CAST(od.QUANTITY AS DOUBLE)\n" +
            "      * CAST(REPLACE(od.Unit_Price, ',', '.') AS DOUBLE)\n" +
            "      * (1.0 - CAST(REPLACE(COALESCE(od.DISCOUNT, '0'), ',', '.') AS DOUBLE))\n" +
            "    ) AS order_total\n" +
            "  FROM \"ORDER\" o JOIN ORDERDETAILS od ON od.Order_ID = o.Order_ID\n" +
            "  GROUP BY o.Customer_ID, o.Order_ID\n" +
            ")\n" +
            "SELECT c.First_Name AS client, MOYENNE(ot.order_total) AS moyenne" + ecart + "\n" +
            "FROM CUSTOMER c JOIN order_totals ot ON ot.customer_id = c.ID\n" +
            "GROUP BY c.First_Name ORDER BY c.First_Name";

        List<String> headers = new ArrayList<>(List.of("CLIENT", "MOYENNE"));
        if (avecEcart) headers.add("ECARTTYPE");

        List<List<String>> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(rs.getString("client"));
                row.add(String.format(Locale.US, "%.2f", rs.getDouble("moyenne")));
                if (avecEcart) row.add(String.format(Locale.US, "%.2f", rs.getDouble("ecarttype")));
                rows.add(row);
            }
        }

        afficherTableau(headers, rows);
    }

    private static void afficherTableau(List<String> headers, List<List<String>> rows) {
        int[] w = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            w[i] = headers.get(i).length();
            for (List<String> row : rows)
                w[i] = Math.max(w[i], row.get(i).length());
        }

        String sep = separateur(w);
        System.out.println(sep);
        System.out.println(ligne(headers, w));
        System.out.println(sep);
        for (List<String> row : rows) System.out.println(ligne(row, w));
        System.out.println(sep);
    }

    private static String separateur(int[] w) {
        StringBuilder sb = new StringBuilder("+");
        for (int wi : w) sb.append("-".repeat(wi + 2)).append("+");
        return sb.toString();
    }

    private static String ligne(List<String> cells, int[] w) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.size(); i++)
            sb.append(String.format(" %-" + w[i] + "s |", cells.get(i)));
        return sb.toString();
    }
}
