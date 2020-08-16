//
// Created by 谭华 on 2020/5/12.
//

#include <jni.h>
#include "./cradle_rancune_tech_jni_JniTest.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_cradle_rancune_tech_jni_JniTest_plus(JNIEnv *env, jclass clas, jint a, jint b) {
    return a + b;
}

JNIEXPORT jint JNICALL
Java_cradle_rancune_tech_jni_JniTest_minus(JNIEnv *env, jclass clazz, jint a, jint b) {
    jclass intArrayClass = (*env)->FindClass(env, "[I");
    jobjectArray array = (*env)->NewObjectArray(env, 10, intArrayClass, NULL);
    return a - b;
}

JNIEXPORT void JNICALL
Java_cradle_rancune_tech_jni_JniTest_print(JNIEnv *env, jobject thiz, jstring string) {

}

JNIEXPORT jstring JNICALL
Java_cradle_rancune_tech_jni_JniTest_getNativeString(JNIEnv *env, jclass clazz, jstring string) {
    // 将 jstring 类型的字符串转换为 C 风格的字符串，会额外申请内存
    const char * str = (*env) -> GetStringUTFChars(env, string, JNI_FALSE);
    // 使用完后，需要释放掉申请的 C 风格字符串的内存
    (*env) -> ReleaseStringUTFChars(env, string, str);
    // 生成 jstring 类型的字符串
    jstring returnValue = (*env) -> NewStringUTF(env, "Hello World");
    return returnValue;
}

#ifdef __cplusplus
}
#endif
