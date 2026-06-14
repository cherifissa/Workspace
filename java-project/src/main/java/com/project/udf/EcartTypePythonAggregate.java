package com.project.udf;

import com.project.NativeStats;
import org.h2.api.AggregateFunction;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EcartTypePythonAggregate implements AggregateFunction {

    private final List<Double> values = new ArrayList<>();

    @Override public void init(Connection c) { values.clear(); }
    @Override public int getType(int[] t)    { return Types.DOUBLE; }

    @Override
    public void add(Object value) {
        if (value != null) values.add(((Number) value).doubleValue());
    }

    @Override
    public Object getResult() {
        if (values.isEmpty()) return 0.0;
        double[] arr = new double[values.size()];
        for (int i = 0; i < values.size(); i++) arr[i] = values.get(i);
        return NativeStats.ecartTypeNumpy(arr);
    }
}
