#include "bridge.h"
#include <cmath>
#include <vector>
#include <range/v3/numeric/accumulate.hpp>

namespace
{
    std::vector<double> toVector(JNIEnv *env, jdoubleArray values)
    {
        std::vector<double> out;
        if (values == nullptr)
        {
            return out;
        }

        jsize n = env->GetArrayLength(values);
        out.resize(static_cast<std::size_t>(n));
        env->GetDoubleArrayRegion(values, 0, n, out.data());
        return out;
    }

    double moyenne(const std::vector<double> &values)
    {
        if (values.empty())
        {
            return 0.0;
        }
        double sum = ranges::accumulate(values, 0.0);
        return sum / static_cast<double>(values.size());
    }
}

JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_moyenneNative(JNIEnv *env, jclass, jdoubleArray values)
{
    auto vec = toVector(env, values);
    return moyenne(vec);
}

JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_ecartTypeNative(JNIEnv *env, jclass, jdoubleArray values)
{
    auto vec = toVector(env, values);
    if (vec.empty())
    {
        return 0.0;
    }

    double avg = moyenne(vec);
    double varianceSum = 0.0;
    for (double v : vec)
    {
        double d = v - avg;
        varianceSum += d * d;
    }
    double variance = varianceSum / static_cast<double>(vec.size());
    return std::sqrt(variance);
}