package com.project;

import org.h2.command.ddl.CreateTableData;
import org.h2.command.query.AllColumnsForPlan;
import org.h2.engine.SessionLocal;
import org.h2.index.Cursor;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.DefaultRow;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.*;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2.value.ValueVarchar;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvVirtualTable extends TableBase {

    private final List<Value[]> rows = new ArrayList<>();
    private final Index scanIndex;

    public CsvVirtualTable(CreateTableData data, String csvFile) {
        super(data);
        loadCsv(csvFile);
        scanIndex = new CsvScanIndex(this, rows);
    }

    private void loadCsv(String fileName) {
        Path path = resolveCsv(fileName);
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);

            if (lines.isEmpty()) return;

            String headerLine = lines.get(0);
            if (headerLine.startsWith("﻿")) headerLine = headerLine.substring(1);
            String[] headers = headerLine.split(";", -1);

            Column[] cols = new Column[headers.length];
            for (int i = 0; i < headers.length; i++) {
                String name = headers[i].trim().replaceAll("[\\s\\-/]", "_");
                cols[i] = new Column(name, TypeInfo.TYPE_VARCHAR);
            }
            setColumns(cols);

            for (int r = 1; r < lines.size(); r++) {
                String[] fields = lines.get(r).split(";", -1);
                Value[] values = new Value[headers.length];
                for (int c = 0; c < headers.length; c++) {
                    String v = (c < fields.length) ? fields[c].trim() : "";
                    values[c] = ValueVarchar.get(v);
                }
                rows.add(values);
            }
        } catch (IOException e) {
            throw DbException.convert(e);
        }
    }

    private static Path resolveCsv(String name) {
        for (Path p : List.of(
                Path.of("src/main/resources/data", name),
                Path.of("java-project/src/main/resources/data", name),
                Path.of("../java-project/src/main/resources/data", name))) {
            if (Files.exists(p)) return p;
        }
        throw new IllegalStateException("CSV introuvable : " + name);
    }

    @Override public Index getScanIndex(SessionLocal session) { return scanIndex; }
    @Override public ArrayList<Index> getIndexes() { return new ArrayList<>(List.of(scanIndex)); }
    @Override public TableType getTableType()       { return TableType.EXTERNAL_TABLE_ENGINE; }
    @Override public boolean canGetRowCount(SessionLocal session) { return true; }
    @Override public long getRowCount(SessionLocal session)       { return rows.size(); }
    @Override public long getRowCountApproximation(SessionLocal session) { return rows.size(); }
    @Override public long getMaxDataModificationId() { return 0L; }
    @Override public boolean isDeterministic()       { return true; }
    @Override public boolean canDrop()               { return true; }
    @Override public void close(SessionLocal session) {}
    @Override public void checkSupportAlter() { throw DbException.getUnsupportedException("ALTER"); }
    @Override public void addRow(SessionLocal s, Row r)    { throw DbException.getUnsupportedException("INSERT"); }
    @Override public void removeRow(SessionLocal s, Row r) { throw DbException.getUnsupportedException("DELETE"); }
    @Override public long truncate(SessionLocal s) { throw DbException.getUnsupportedException("TRUNCATE"); }
    @Override public Index addIndex(SessionLocal s, String n, int id, IndexColumn[] c, int u,
                                    IndexType t, boolean cr, String cm) {
        throw DbException.getUnsupportedException("CREATE INDEX");
    }

    private static final class CsvScanIndex extends Index {

        private final List<Value[]> rows;

        CsvScanIndex(Table table, List<Value[]> rows) {
            super(table, 0, table.getName() + "_SCAN",
                  IndexColumn.wrap(table.getColumns()), 0, IndexType.createScan(false));
            this.rows = rows;
        }

        @Override
        public Cursor find(SessionLocal session, SearchRow first, SearchRow last) {
            return new CsvCursor(rows);
        }

        @Override
        public double getCost(SessionLocal session, int[] masks, TableFilter[] filters,
                              int firstColumn, SortOrder sortOrder, AllColumnsForPlan allCols) {
            return rows.size() + 1.0;
        }

        @Override public long getRowCount(SessionLocal s)             { return rows.size(); }
        @Override public long getRowCountApproximation(SessionLocal s){ return rows.size(); }
        @Override public void close(SessionLocal s)    {}
        @Override public void add(SessionLocal s, Row r)    {}
        @Override public void remove(SessionLocal s, Row r) {}
        @Override public void remove(SessionLocal s)        {}
        @Override public void truncate(SessionLocal s)      {}
        @Override public boolean needRebuild()              { return false; }
    }

    private static final class CsvCursor implements Cursor {

        private final List<Value[]> rows;
        private int pos = -1;
        private Row current;

        CsvCursor(List<Value[]> rows) { this.rows = rows; }

        @Override public Row get()            { return current; }
        @Override public SearchRow getSearchRow() { return current; }

        @Override
        public boolean next() {
            if (++pos < rows.size()) {
                current = Row.get(rows.get(pos), DefaultRow.MEMORY_CALCULATE);
                return true;
            }
            return false;
        }

        @Override public boolean previous() { return false; }
    }
}
