#include <jni.h>
#include <filesystem>
#include <iostream>
#include <string>

namespace
{
    std::string findJarPath()
    {
        const std::string name = "java-project-1.0-jar-with-dependencies.jar";
        const std::filesystem::path p1 = std::filesystem::path("../../java-project/target") / name;
        const std::filesystem::path p2 = std::filesystem::path("java-project/target") / name;
        const std::filesystem::path p3 = std::filesystem::path("../java-project/target") / name;
        if (std::filesystem::exists(p1))
        {
            return p1.string();
        }
        if (std::filesystem::exists(p2))
        {
            return p2.string();
        }
        if (std::filesystem::exists(p3))
        {
            return p3.string();
        }
        return p1.string();
    }

    std::string findLibraryPath()
    {
        const std::filesystem::path p1 = std::filesystem::path(".");
        const std::filesystem::path p2 = std::filesystem::path("cpp-project/build");
        if (std::filesystem::exists(p1 / "libbridge.dylib") || std::filesystem::exists(p1 / "libbridge.so"))
        {
            return p1.string();
        }
        return p2.string();
    }
}

int main()
{
    JavaVM *jvm = nullptr;
    JNIEnv *env = nullptr;

    JavaVMInitArgs args;
    JavaVMOption options[2];

    const std::string classpathOpt = "-Djava.class.path=" + findJarPath();
    const std::string libPathOpt = "-Djava.library.path=" + findLibraryPath();

    args.version = JNI_VERSION_1_8;
    args.nOptions = 2;
    options[0].optionString = const_cast<char *>(classpathOpt.c_str());
    options[1].optionString = const_cast<char *>(libPathOpt.c_str());
    args.options = options;
    args.ignoreUnrecognized = JNI_FALSE;

    if (JNI_CreateJavaVM(&jvm, reinterpret_cast<void **>(&env), &args) != JNI_OK)
    {
        std::cerr << "Failed to create JVM" << std::endl;
        return 1;
    }

    jclass mainCls = env->FindClass("com/project/Main");
    if (mainCls == nullptr)
    {
        std::cerr << "Class com/project/Main not found" << std::endl;
        jvm->DestroyJavaVM();
        return 1;
    }

    jmethodID q4 = env->GetStaticMethodID(mainCls, "afficherMoyenneEtEcartType", "()V");
    if (q4 == nullptr)
    {
        std::cerr << "Method afficherMoyenneEtEcartType not found" << std::endl;
        jvm->DestroyJavaVM();
        return 1;
    }

    env->CallStaticVoidMethod(mainCls, q4);
    if (env->ExceptionCheck())
    {
        env->ExceptionDescribe();
        env->ExceptionClear();
        jvm->DestroyJavaVM();
        return 1;
    }

    jclass reqCls = env->FindClass("com/project/proto/FilterRequestOuterClass$FilterRequest");
    jclass builderCls = env->FindClass("com/project/proto/FilterRequestOuterClass$FilterRequest$Builder");

    jmethodID newBuilder = env->GetStaticMethodID(
        reqCls,
        "newBuilder",
        "()Lcom/project/proto/FilterRequestOuterClass$FilterRequest$Builder;");
    jobject builder = env->CallStaticObjectMethod(reqCls, newBuilder);

    jmethodID addCustomers = env->GetMethodID(
        builderCls,
        "addCustomers",
        "(Ljava/lang/String;)Lcom/project/proto/FilterRequestOuterClass$FilterRequest$Builder;");
    jmethodID setMinTotal = env->GetMethodID(
        builderCls,
        "setMinTotal",
        "(D)Lcom/project/proto/FilterRequestOuterClass$FilterRequest$Builder;");
    jmethodID build = env->GetMethodID(
        builderCls,
        "build",
        "()Lcom/project/proto/FilterRequestOuterClass$FilterRequest;");

    jstring n1 = env->NewStringUTF("Anna");
    jstring n2 = env->NewStringUTF("Christina");
    env->CallObjectMethod(builder, addCustomers, n1);
    env->CallObjectMethod(builder, addCustomers, n2);
    env->CallObjectMethod(builder, setMinTotal, 200.0);

    jobject request = env->CallObjectMethod(builder, build);

    jmethodID q5 = env->GetStaticMethodID(
        mainCls,
        "afficherMoyenneEtEcartType2",
        "(Lcom/project/proto/FilterRequestOuterClass$FilterRequest;)V");

    env->CallStaticVoidMethod(mainCls, q5, request);
    if (env->ExceptionCheck())
    {
        env->ExceptionDescribe();
        env->ExceptionClear();
        jvm->DestroyJavaVM();
        return 1;
    }

    return jvm->DestroyJavaVM();
}