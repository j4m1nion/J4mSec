//
// Created by mamil on 05/07/2025.
//

#ifndef J4MSEC_APP_SIGNATURE_H
#define J4MSEC_APP_SIGNATURE_H

#include <jni.h>

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManager_verifyAppSignatureAsByteArray(
        JNIEnv *env, jobject thiz, jbyteArray certificate_hash, jstring app_hash);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManager_verifyAppSignatureAsString(
        JNIEnv *env, jobject thiz, jstring certificate_hash, jstring app_hash);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManager_verifyAppSignatureAsStringWithXorAndSalt(
        JNIEnv *env, jobject thiz, jstring certificate_hash, jchar xor_key, jboolean reverse,
        jstring salt, jstring app_hash);



#endif //J4MSEC_APP_SIGNATURE_H
