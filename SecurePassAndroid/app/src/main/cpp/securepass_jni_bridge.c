#include <jni.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "SecurePassEngine.h"

JNIEXPORT jint JNICALL
Java_com_nejracoric_securepassandroid_native_SecurePassNative_nativeGeneratePassword(
        JNIEnv *env,
        jobject thiz,
        jint length,
        jint include_special,
        jbyteArray buffer,
        jint buffer_size) {
    if (buffer == NULL || buffer_size <= 0) {
        return -1;
    }

    jbyte *bytes = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (bytes == NULL) {
        return -1;
    }

    int32_t status = securepass_generate_password(
        (int32_t) length,
        (int32_t) include_special,
        (char *) bytes,
        (int32_t) buffer_size);

    (*env)->ReleaseByteArrayElements(env, buffer, bytes, 0);
    return (jint) status;
}

JNIEXPORT jdouble JNICALL
Java_com_nejracoric_securepassandroid_native_SecurePassNative_nativeCalculateEntropy(
        JNIEnv *env,
        jobject thiz,
        jbyteArray password_bytes) {
    if (password_bytes == NULL) {
        return 0.0;
    }

    jsize len = (*env)->GetArrayLength(env, password_bytes);
    if (len <= 0) {
        return 0.0;
    }

    jbyte *bytes = (*env)->GetByteArrayElements(env, password_bytes, NULL);
    if (bytes == NULL) {
        return 0.0;
    }

    char *copy = (char *) malloc((size_t) len + 1);
    if (copy == NULL) {
        (*env)->ReleaseByteArrayElements(env, password_bytes, bytes, JNI_ABORT);
        return 0.0;
    }
    memcpy(copy, bytes, (size_t) len);
    copy[len] = '\0';

    double entropy = securepass_calculate_entropy(copy);

    free(copy);
    (*env)->ReleaseByteArrayElements(env, password_bytes, bytes, JNI_ABORT);
    return (jdouble) entropy;
}
