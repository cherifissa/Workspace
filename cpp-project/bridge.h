#include <jni.h>

extern "C"
{
    JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_ecartTypeNative(JNIEnv *, jclass, jdoubleArray);
    JNIEXPORT jdouble JNICALL Java_com_project_NativeStats_ecartTypeNumpy(JNIEnv *, jclass, jdoubleArray);
}
