package com.project;

public final class NativeStats {

    static {
        System.loadLibrary("bridge");
    }

    private NativeStats() {
    }

    public static native double ecartTypeNative(double[] values);

    public static native double ecartTypeNumpy(double[] values);
}
