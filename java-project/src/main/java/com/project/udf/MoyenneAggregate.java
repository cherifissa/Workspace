package com.project.udf;

import org.h2.api.AggregateFunction;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MoyenneAggregate implements AggregateFunction {

    private final List<Double> values = new ArrayList<>();

    @Override public void init(Connection c) { values.clear(); }
    @Override public int getType(int[] t)    { return Types.DOUBLE; }

    @Override
    public void add(Object value) {
        if (value != null) values.add(((Number) value).doubleValue());
    }

    @Override
    public Object getResult() {
        if (values.isEmpty()) return null;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.size();
    }
}
