package com.project;

import org.h2.api.TableEngine;
import org.h2.command.ddl.CreateTableData;
import org.h2.table.Table;

import java.util.Map;

public class CsvTableEngine implements TableEngine {

    private static final Map<String, String> CSV_MAP = Map.of(
        "CUSTOMER",     "Customers.csv",
        "ORDER",        "Orders.csv",
        "ORDERDETAILS", "OrderDetails.csv"
    );

    @Override
    public Table createTable(CreateTableData data) {
        String csv = CSV_MAP.get(data.tableName.toUpperCase());
        if (csv == null)
            throw new IllegalArgumentException("Table inconnue : " + data.tableName);
        return new CsvVirtualTable(data, csv);
    }
}
