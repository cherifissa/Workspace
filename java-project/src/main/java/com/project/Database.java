package com.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class Database {

    private Database() {}

    public static Connection connect() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL");
        try (Statement s = conn.createStatement()) {
            s.execute("DROP TABLE IF EXISTS ORDERDETAILS");
            s.execute("DROP TABLE IF EXISTS \"ORDER\"");
            s.execute("DROP TABLE IF EXISTS CUSTOMER");
            s.execute("CREATE TABLE CUSTOMER ENGINE \"com.project.CsvTableEngine\"");
            s.execute("CREATE TABLE \"ORDER\" ENGINE \"com.project.CsvTableEngine\"");
            s.execute("CREATE TABLE ORDERDETAILS ENGINE \"com.project.CsvTableEngine\"");
        }
        return conn;
    }
}
