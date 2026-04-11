package com.project;

import java.util.ArrayList;
import java.util.List;

public final class TablePrinter {

    private TablePrinter() {
    }

    public static void print(List<String> headers, List<List<String>> rows) {
        List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int max = headers.get(i).length();
            for (List<String> row : rows) {
                max = Math.max(max, row.get(i).length());
            }
            widths.add(max);
        }

        String border = buildBorder(widths);
        System.out.println(border);
        System.out.println(buildRow(headers, widths));
        System.out.println(border);
        for (List<String> row : rows) {
            System.out.println(buildRow(row, widths));
        }
        System.out.println(border);
    }

    private static String buildBorder(List<Integer> widths) {
        StringBuilder sb = new StringBuilder();
        sb.append('+');
        for (int w : widths) {
            sb.append("-").append("-".repeat(w)).append("-+");
        }
        return sb.toString();
    }

    private static String buildRow(List<String> cells, List<Integer> widths) {
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        for (int i = 0; i < cells.size(); i++) {
            String v = cells.get(i);
            int width = widths.get(i);
            sb.append(' ').append(String.format("%-" + width + "s", v)).append(' ').append('|');
        }
        return sb.toString();
    }
}
