#include <jni.h>
#include <filesystem>
#include <iostream>
#include "main.cpp"
//#include "include/Rtklib/rtklib_fun.h"
#include "include/IO_rtcm.h"
#include <android/log.h>

#define LOG_TAG "YourAppTag"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
using namespace KPL_IO;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_SDKInit(JNIEnv *env, jclass clazz, jstring jmode, jstring jant, jdoubleArray jpos, jdoubleArray jenu, jdouble jcut, jdouble jintv, jstring jtarget_path) {
    const char *mode = env->GetStringUTFChars(jmode, nullptr);
    const char *ant = env->GetStringUTFChars(jant, nullptr);
    jdouble *pos = env->GetDoubleArrayElements(jpos, nullptr);
    jdouble *enu = env->GetDoubleArrayElements(jenu, nullptr);
    const char *path = env->GetStringUTFChars(jtarget_path, nullptr);
    SDK_init(mode, ant, pos, enu, jcut, jintv, path);
    env->ReleaseStringUTFChars(jmode,mode);
    env->ReleaseStringUTFChars(jant,ant);
    env->ReleaseDoubleArrayElements(jpos,pos,0);
    env->ReleaseDoubleArrayElements(jenu,enu,0);
    env->ReleaseStringUTFChars(jtarget_path,path);
}

extern "C"
JNIEXPORT jint
JNICALL
        Java_com_example_fortest_SDK_IOInputObsData(JNIEnv * env, jclass
clazz,
jbyte data
) {
// TODO: implement IOInputObsData()
return IO_inputObsData(data);
}

extern "C"
JNIEXPORT jint
JNICALL
        Java_com_example_fortest_SDK_IOInputEphData(JNIEnv * env, jclass
clazz,
jbyte data
) {
// TODO: implement IOInputEphData()
return IO_inputEphData(data);
}

extern "C"
JNIEXPORT jint
JNICALL
        Java_com_example_fortest_SDK_IOInputSsrData(JNIEnv * env, jclass
clazz,
jbyte data
) {
// TODO: implement IOInputSsrData()
return IO_inputSsrData(data);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_SDKSetIntv(JNIEnv *env, jclass clazz, jint intv) {
    SDK_setIntv(intv);
//    double ref_pos[3] = {1227856.3104, -4698500.7901, 4079976.9345};
//    double point_pos[3] = {1227856.3204, -4698499.7901, 4079977.9345};
//    double enu[3];
//    SDK_ecef2enu(ref_pos,point_pos,enu);
//    LOGD("1111 %f %f %f",enu[0],enu[1],enu[2]);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_fortest_SDK_SDKRetrieve(JNIEnv *env, jclass clazz, jstring jtype, jint len) {
    // TODO: implement SDKRetrieve()
    const char * type = (env)->GetStringUTFChars(jtype, nullptr);
    char buff[1024000];

    SDK_retrieve(type, buff, len);
    env->ReleaseStringUTFChars(jtype,type);
//    LOGD("12345%s", (*env).NewStringUTF( buff));
    return (*env).NewStringUTF( buff);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_SDKTerminate(JNIEnv *env, jclass clazz) {
    // TODO: implement SDKTerminate()
    SDK_terminate();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_SDKSetpath(JNIEnv *env, jclass clazz, jstring jpath) {
    // TODO: implement SDKSetpath()
    const char *path = env->GetStringUTFChars(jpath, nullptr);
    SDK_setpath(path);
    env->ReleaseStringUTFChars(jpath, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_SDKRestart(JNIEnv *env, jclass clazz) {
    // TODO: implement SDKRestart()
    SDK_restart();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_sendEphData(JNIEnv *env, jclass clazz, jbyteArray jByteArray) {
    // 获取字节数组的长度
    jsize len = (env)->GetArrayLength(jByteArray);

    // 获取字节数组的元素
    jbyte *bytes = (env)->GetByteArrayElements(jByteArray, 0);

    // 创建一个普通的字节数组并拷贝数据
    // 这里使用了 C 语言中的动态内存分配，你可以根据需要使用其他方式
    unsigned char *nativeByteArray = (unsigned char *) malloc(len);
    memcpy(nativeByteArray, bytes, len);

    // 释放字节数组的元素
    (env)->ReleaseByteArrayElements(jByteArray, bytes, 0);

    for (int i = 0; i < len; ++i) {
        IO_inputEphData(nativeByteArray[i]);
    }
    // 释放 nativeByteArray 的内存
    free(nativeByteArray);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_sendSsrData(JNIEnv *env, jclass clazz, jbyteArray jByteArray) {
    // TODO: implement sendSsrData()
    // 获取字节数组的长度
    jsize len = (env)->GetArrayLength(jByteArray);

    // 获取字节数组的元素
    jbyte *bytes = (env)->GetByteArrayElements(jByteArray, 0);

    unsigned char *nativeByteArray = (unsigned char *) malloc(len);
    memcpy(nativeByteArray, bytes, len);

    // 释放字节数组的元素
    (env)->ReleaseByteArrayElements(jByteArray, bytes, 0);

    // 在这里你可以使用 nativeByteArray，然后记得在使用后释放内存
    // ...

    for (int i = 0; i < len; ++i) {
        IO_inputSsrData(nativeByteArray[i]);
    }
    // 释放 nativeByteArray 的内存
    free(nativeByteArray);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_fortest_SDK_sendObsData(JNIEnv *env, jclass clazz, jbyteArray jByteArray) {
    // TODO: implement sendSsrData()
    // 获取字节数组的长度
    jsize len = (env)->GetArrayLength(jByteArray);

    // 获取字节数组的元素
    jbyte *bytes = (env)->GetByteArrayElements(jByteArray, 0);

    unsigned char *nativeByteArray = (unsigned char *) malloc(len);
    memcpy(nativeByteArray, bytes, len);

    // 释放字节数组的元素
    (env)->ReleaseByteArrayElements(jByteArray, bytes, 0);

    // 在这里你可以使用 nativeByteArray，然后记得在使用后释放内存
    // ...
    char buff_r[1024] = {0};
    for (int i = 0; i < len; ++i) {
        if (1 == IO_inputObsData(nativeByteArray[i])) {
            SDK_retrieve("NMEA_GGA", buff_r, 104);
            LOGD("%s\n", buff_r);
        }
    }
    // 释放 nativeByteArray 的内存
    free(nativeByteArray);
}