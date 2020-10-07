//
// Created by 谭华 on 2020/5/12.
//
#include "cradle_rancune_tech_jni_JniTest.h"
#include "logutils.h"
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

jclass catClass;
jmethodID catConstructor;

void Java_cradle_rancune_tech_jni_JniTest_cacheFieldAndMethods(JNIEnv *env, jclass clazz) {
    catClass = (*env)->FindClass(env, "cradle/rancune/tech/jni/Cat");
    catConstructor = (*env)->GetMethodID(env, catClass, "<init>", "(Ljava/lang/String;)V");
}

JNIEXPORT jstring JNICALL
Java_cradle_rancune_tech_jni_JniTest_getNativeString(JNIEnv *env, jclass clazz, jstring string) {
    LOGD("getNativeString");
    // jstring是指向jvm内部的字符串，它不是c风格的字符串char*
    // 将jstring 类型的字符串转换为C风格的UTF-8字符串，会额外申请内存
    if (string == NULL) {
        return NULL;
    }
    int length = (*env)->GetStringUTFLength(env, string);
    LOGD("getNativeString, origin length:%d", length)
    if (length > 4) {
        jchar outbuf[4];
        (*env)->GetStringUTFRegion(env, string, 0, 4, outbuf);
        LOGD("%s", outbuf);
    }
    const char *str = (*env)->GetStringUTFChars(env, string, NULL);
    if (str != NULL) {
        printf("%s", str);
    }
    // 使用完后，需要释放掉申请的 C 风格字符串的内存
    (*env)->ReleaseStringUTFChars(env, string, str);
    // 生成jstring类型的字符串
    jstring returnValue = (*env)->NewStringUTF(env, "Hello World from getNativeString");
    return returnValue;
}

jint
Java_cradle_rancune_tech_jni_JniTest_intArraySum(JNIEnv *env, jclass clazz, jintArray intArray_,
                                                 jint num_) {
    LOGD("intArraySum");
    jint *intArray = (*env)->GetIntArrayElements(env, intArray_, NULL);
    if (intArray == NULL) {
        return 0;
    }
    int sum = 0;
    // 如同getUTFString一样，会申请native内存
    int length = (*env)->GetArrayLength(env, intArray_);
    LOGD("array length is %d", length);
    for (int i = 0; i < length; i++) {
        sum += intArray[i];
    }
    LOGD("sum is %d", sum);
    // 用完之后需要释放
    (*env)->ReleaseIntArrayElements(env, intArray_, intArray, 0);

    // GetIntArrayRegin获得数组的内容
    jint buf[num_];
    // 将数组内容复制到 C 缓冲区内，或将缓冲区内的内容复制到数组上
    (*env)->GetIntArrayRegion(env, intArray_, 0, num_, buf);
    sum = 0;
    for (int i = 0; i < num_; i++) {
        sum += buf[i];
    }
    LOGD("sum is %d", sum);
    return 0;
}

jintArray Java_cradle_rancune_tech_jni_JniTest_getIntArray(JNIEnv *env, jclass clazz, jint num_) {
    LOGD("getIntArray");
    if (num_ <= 0) {
        return NULL;
    }
    // NewTypeArray,返回一个指定数据类型的数组
    jintArray array = (*env)->NewIntArray(env, num_);
    jint buf[num_];
    for (int i = 0; i < num_; i++) {
        buf[i] = i * 2;
    }
    // 通过 SetTypeArrayRegion 来给指定类型数组赋值
    (*env)->SetIntArrayRegion(env, array, 0, num_, buf);
    return array;
}

jobjectArray
Java_cradle_rancune_tech_jni_JniTest_getTwoDimensionalArray(JNIEnv *env, jclass clazz,
                                                            jint row, jint column) {
    LOGD("getTwoDimensionalArray");
    // 二维数组，可以看作数组的数组
    jobjectArray result;
    // int[]类型
    jclass intArrayClass = (*env)->FindClass(env, "[I");
    if (intArrayClass == NULL) {
        return NULL;
    }
    // 相当于初始化一个对象数组，用指定的对象类型
    result = (*env)->NewObjectArray(env, row, intArrayClass, NULL);
    if (result == NULL) {
        return NULL;
    }
    for (int i = 0; i < row; i++) {
        // 声明一个整型数组
        jintArray arr = (*env)->NewIntArray(env, column);
        if (arr == NULL) {
            return NULL;
        }
        // 用来给整型数组填充数据的缓冲区
        jint tmp[column];
        for (int j = 0; j < column; ++j) {
            tmp[j] = i * 10 + j;
        }
        // 给整型数组填充数据
        (*env)->SetIntArrayRegion(env, arr, 0, column, tmp);
        // 给对象数组指定位置填充数据，这个数据就是一个一维整型数组
        (*env)->SetObjectArrayElement(env, result, i, arr);
        // 释放局部引用
        (*env)->DeleteLocalRef(env, arr);
    }
    return result;
}

void Java_cradle_rancune_tech_jni_JniTest_printAnimalsName(JNIEnv *env, jclass clazz,
                                                           jobjectArray beans) {
    LOGD("printAnimalsName");
    // 数组长度
    int size = (*env)->GetArrayLength(env, beans);
    if (size <= 0) {
        return;
    }
    // 数组中对应的类
    jclass class = (*env)->FindClass(env, "cradle/rancune/tech/jni/Animal");
    // 类对应的字段描述
    jfieldID jfieldId = (*env)->GetFieldID(env, class, "name", "Ljava/lang/String;");
    // 类的字段具体的值
    jstring jstr;
    // 类字段具体值转换成 C/C++ 字符串
    const char *str;
    jobject animal;
    for (int i = 0; i < size; i++) {
        // 得到数组中的每一个元素
        animal = (*env)->GetObjectArrayElement(env, beans, i);
        // 每一个元素具体字段的值
        jstr = (*env)->GetObjectField(env, animal, jfieldId);
        str = (*env)->GetStringUTFChars(env, jstr, NULL);
        if (str == NULL) {
            continue;
        }
        LOGD("animal %d name is: %s", i, str);
        (*env)->ReleaseStringUTFChars(env, jstr, str);
    }
}

/**
 * 1. 获取 Java 对象的类
 * 2. 获取对应字段的 id
 * 3. 获取具体的字段值
 */
void Java_cradle_rancune_tech_jni_JniTest_accessInstanceAndStaticField(JNIEnv *env, jobject thiz,
                                                                       jobject animal) {
    LOGD("accessInstanceField");
    // 想要获取的字段 id
    jfieldID fid;
    // 字段对应的具体的值
    jstring jstr;
    const char *str;

    // 通过 GetObjectClass 函数获取对应的 Java 类
    jclass class = (*env)->GetObjectClass(env, animal);
    // 通过 GetFieldID 方法获得 Java 类型对应的字段 id
    fid = (*env)->GetFieldID(env, class, "name", "Ljava/lang/String;");
    if (fid == NULL) {
        return;
    }
    // 得到了 Java 类型和字段的 id 后，就可以通过 GetObjectField 方法来获取具体的值
    // 基础类型还有GetBooleanField、GetIntField、GetDoubleField
    jstr = (*env)->GetObjectField(env, animal, fid);
    str = (*env)->GetStringUTFChars(env, jstr, NULL);
    if (str == NULL) {
        return;
    }
    LOGD("name is %s", str);
    (*env)->ReleaseStringUTFChars(env, jstr, str);
    jstr = (*env)->NewStringUTF(env, "accessInstanceAndStaticField");
    if (jstr == NULL) {
        return;
    }
    // SetObjectField设置值
    // SetBooleanField、SetCharField、SetDoubleField
    (*env)->SetObjectField(env, animal, fid, jstr);

    // static 成员变量
    jfieldID staticFid = (*env)->GetStaticFieldID(env, class, "num", "I");
    if (staticFid == NULL) {
        return;
    }
    jint num = (*env)->GetStaticIntField(env, class, staticFid);
    LOGD("static num is %d", num);
    (*env)->SetStaticIntField(env, class, staticFid, ++num);
}

void Java_cradle_rancune_tech_jni_JniTest_callInstanceAndStaticMethod(JNIEnv *env, jobject thiz,
                                                                      jobject animal) {
    jclass class = (*env)->GetObjectClass(env, animal);
    jmethodID mid = (*env)->GetMethodID(env, class, "setName",
                                        "(Ljava/lang/String;)Lcradle/rancune/tech/jni/Animal;");
    if (mid == NULL) {
        return;
    }
    jstring jstr = (*env)->NewStringUTF(env, "invoke instance method");
    (*env)->CallObjectMethod(env, animal, mid, jstr);

    // static 成员方法
    jmethodID staticMid = (*env)->GetStaticMethodID(env, class, "setNum",
                                                    "(I)V");
    if (staticMid == NULL) {
        return;
    }
    (*env)->CallStaticVoidMethod(env, class, staticMid, 10000);
}

/**
 * 构造函数的名字比较特殊为
 * <init>
 * 构造函数的返回值为Void
 */
jobject Java_cradle_rancune_tech_jni_JniTest_invokeAnimalConstructor(JNIEnv *env, jobject thiz,
                                                                     jstring name) {
    jclass jclass = (*env)->FindClass(env, "cradle/rancune/tech/jni/Animal");
    if (jclass == NULL) {
        return NULL;
    }
    jmethodID mid = (*env)->GetMethodID(env, jclass, "<init>", "(Ljava/lang/String;)V");
    if (mid == NULL) {
        return NULL;
    }
    jobject obj = (*env)->NewObject(env, jclass, mid, name);
    //
    // 另外一种调用AllocObject创建对象创建对象，此时创建的对象未初始化的对象
    jobject obj1 = (*env)->AllocObject(env, jclass);
    if (obj1 != NULL) {
        // 调用 CallNonvirtualVoidMethod 方法去调用类的构造方法
        (*env)->CallNonvirtualVoidMethod(env, obj1, jclass, mid, name);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->DeleteLocalRef(env, obj1);
        }
    }

    return obj;
}

jobject Java_cradle_rancune_tech_jni_JniTest_allocAnimalConstructor(JNIEnv *env, jobject thiz,
                                                                    jstring name) {
    jclass jclass = (*env)->FindClass(env, "cradle/rancune/tech/jni/Animal");
    if (jclass == NULL) {
        return NULL;
    }
    jmethodID mid = (*env)->GetMethodID(env, jclass, "<init>", "(Ljava/lang/String;)V");
    if (mid == NULL) {
        return NULL;
    }
    // 另外一种调用AllocObject创建对象创建对象，此时创建的对象未初始化的对象
    jobject obj = (*env)->AllocObject(env, jclass);
    if (obj != NULL) {
        // 调用 CallNonvirtualVoidMethod 方法去调用类的构造方法
        (*env)->CallNonvirtualVoidMethod(env, obj, jclass, mid, name);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->DeleteLocalRef(env, obj);
            return NULL;
        }
    }

    return obj;
}

void Java_cradle_rancune_tech_jni_JniTest_callSuperMethod(JNIEnv *env, jobject thiz, jstring name) {
    jclass catClazz = (*env)->FindClass(env, "cradle/rancune/tech/jni/Cat");
    if (catClazz == NULL) {
        return;
    }
    jmethodID catMid = (*env)->GetMethodID(env, catClazz, "<init>", "(Ljava/lang/String;)V");
    jobject cat = (*env)->NewObject(env, catClazz, catMid, name);
    if (cat == NULL) {
        return;
    }
    jclass animalClass = (*env)->FindClass(env, "cradle/rancune/tech/jni/Animal");
    if (animalClass == NULL) {
        return;
    }
    jmethodID animalMid = (*env)->GetMethodID(env, animalClass, "getName", "()Ljava/lang/String;");
    if (animalMid == NULL) {
        return;
    }
    // 在cat类中调用animal的getName方法
    jstring catName = (*env)->CallNonvirtualObjectMethod(env, cat, animalClass, animalMid);
    LOGD("cat name: %s", catName);
    jmethodID animalMid2 = (*env)->GetMethodID(env, animalClass, "method1", "()V");
    if (animalMid2 == NULL) {
        return;
    }
    (*env)->CallNonvirtualVoidMethod(env, cat, animalClass, animalMid2);
}


JNIEXPORT jobject JNICALL
Java_cradle_rancune_tech_jni_JniTest_useCacheMethod(JNIEnv *env, jobject thiz, jstring name) {
    if (catClass != NULL && catConstructor != NULL) {
        jobject cat = (*env)->NewObject(env, catClass, catConstructor, name);
        return cat;
    }
    return NULL;
}

#ifdef __cplusplus
}
#endif
