package com.project;

import java.util.List;

public class StatsFunctions {

    public static Double moyenne(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        double sum = 0;
        for (Double v : values) {
            if (v != null) {
                sum += v;
            }
        }
        return sum / values.size();
    }

    public static Double ecartTypePython(List<Double> values) throws Exception {
        return PythonBridge.ecartType(values);
    }
}