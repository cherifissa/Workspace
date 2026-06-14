#include "bridge.h"
#include <Python.h>
#include <cmath>
#include <vector>
#include <range/v3/numeric/accumulate.hpp>

namespace
{
    std::vector<double> toVector(JNIEnv *env, jdoubleArray values)
    {
        std::vector<double> out;
        if (values == nullptr)
            return out;
        jsize n = env->GetArrayLength(values);
        out.resize(static_cast<std::size_t>(n));
        env->GetDoubleArrayRegion(values, 0, n, out.data());
        return out;
    }

    double moyenne(const std::vector<double> &v)
    {
        if (v.empty()) return 0.0;
        return ranges::accumulate(v, 0.0) / static_cast<double>(v.size());
    }
}

JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_ecartTypeNative(JNIEnv *env, jclass, jdoubleArray values)
{
    auto vec = toVector(env, values);
    if (vec.empty()) return 0.0;

    double avg = moyenne(vec);
    double sum = 0.0;
    for (double v : vec)
        sum += (v - avg) * (v - avg);
    return std::sqrt(sum / static_cast<double>(vec.size()));
}

JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_ecartTypeNumpy(JNIEnv *env, jclass, jdoubleArray values)
{
    auto vec = toVector(env, values);
    if (vec.empty()) return 0.0;

    if (!Py_IsInitialized())
        Py_Initialize();

    PyGILState_STATE gstate = PyGILState_Ensure();

    static bool pathAdded = false;
    if (!pathAdded)
    {
        PyObject *sys  = PyImport_ImportModule("sys");
        PyObject *path = PyObject_GetAttrString(sys, "path");
        for (const char *p : {"src/main/python", "java-project/src/main/python"})
            PyList_Append(path, PyUnicode_FromString(p));
        Py_DECREF(path);
        Py_DECREF(sys);
        pathAdded = true;
    }

    PyObject *statsModule = PyImport_ImportModule("stats");
    PyObject *ecartFn     = PyObject_GetAttrString(statsModule, "ecart_type");

    PyObject *pyList = PyList_New(static_cast<Py_ssize_t>(vec.size()));
    for (std::size_t i = 0; i < vec.size(); i++)
        PyList_SetItem(pyList, static_cast<Py_ssize_t>(i), PyFloat_FromDouble(vec[i]));

    PyObject *pyArgs = PyTuple_Pack(1, pyList);
    PyObject *result = PyObject_Call(ecartFn, pyArgs, nullptr);
    double ret = PyFloat_AsDouble(result);

    Py_XDECREF(result);
    Py_DECREF(pyArgs);
    Py_DECREF(pyList);
    Py_DECREF(ecartFn);
    Py_DECREF(statsModule);

    PyGILState_Release(gstate);
    return ret;
}
