package com.project.udf;

import com.project.StatsFunctions;
import org.h2.api.AggregateFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EcartTypePythonAggregate implements AggregateFunction {

    private final List<Double> values = new ArrayList<>();

    @Override
    public void init(Connection connection) {
        values.clear();
    }

    @Override
    public int getType(int[] inputTypes) {
        return Types.DOUBLE;
    }

    @Override
    public void add(Object value) {
        if (value == null) {
            return;
        }
        values.add(((Number) value).doubleValue());
    }

    @Override
    public Object getResult() throws SQLException {
        try {
            return StatsFunctions.ecartTypePython(values);
        } catch (Exception e) {
            throw new SQLException("Python ECARTTYPE failed", e);
        }
    }
}
