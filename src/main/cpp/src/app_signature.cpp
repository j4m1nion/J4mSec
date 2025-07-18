//
// Created by mamil on 05/07/2025.
//

#include "app_signature.h"
#include "utils.h"



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManagerImpl_verifyAppSignatureAsByteArray(
        JNIEnv *env, jobject thiz, jbyteArray certificate_hash, jstring app_hash) {

    if (certificate_hash == nullptr || app_hash == nullptr) {
        return JNI_FALSE;
    }

    jbyteArray appHash = jstringToJbyteArray(env, app_hash);

    if (appHash == nullptr) {
        return JNI_FALSE;
    }

    jsize appHashLen = env->GetArrayLength(appHash);
    jsize certificateHashLen = env->GetArrayLength(certificate_hash);

    jbyte* appHashBytes = env->GetByteArrayElements(appHash, nullptr);
    jbyte* certificateHashBytes = env->GetByteArrayElements(certificate_hash, nullptr);

    std::string expectedHex = toHexString(appHashBytes, appHashLen);
    std::string actualHex = toHexString(certificateHashBytes, certificateHashLen);

    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "Expected: %s, Actual: %s", expectedHex.c_str(), actualHex.c_str());

    bool appSigningIsValid = appHashLen == certificateHashLen && memcmp(appHashBytes, certificateHashBytes, appHashLen) == 0;

    env->ReleaseByteArrayElements(appHash, appHashBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(certificate_hash, certificateHashBytes, JNI_ABORT);

    return appSigningIsValid ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManagerImpl_verifyAppSignatureAsString(
        JNIEnv *env, jobject thiz, jstring certificate_hash, jstring app_hash) {

    if (certificate_hash == nullptr || app_hash == nullptr) {
        return JNI_FALSE;
    }

    const char *expectedHashC = env ->GetStringUTFChars(certificate_hash, nullptr);
    std::string expectedHash(expectedHashC);
    env ->ReleaseStringUTFChars(certificate_hash, expectedHashC);

    const char *actualHashC = env ->GetStringUTFChars(app_hash, nullptr);
    std::string actualHash(actualHashC);
    env ->ReleaseStringUTFChars(app_hash, actualHashC);
    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "Expected: %s, Actual: %s", expectedHash.c_str(), actualHash.c_str());

    return (actualHash == expectedHash) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_appsignatureverification_AppSignatureManagerImpl_verifyAppSignatureAsStringWithXorAndSalt(
        JNIEnv *env, jobject thiz, jstring certificate_hash, jchar xor_key, jboolean reverse,
        jstring salt, jstring app_hash) {
    if (certificate_hash == nullptr || app_hash == nullptr) {
        return JNI_FALSE;
    }

    const char *appHashC = env ->GetStringUTFChars(app_hash, nullptr);
    std::string appHash(appHashC);
    env ->ReleaseStringUTFChars(app_hash, appHashC);

    const char *expectedHashC = env ->GetStringUTFChars(certificate_hash, nullptr);
    std::string expectedHash(expectedHashC);
    env ->ReleaseStringUTFChars(certificate_hash, expectedHashC);


    std::string saltedString;
    if (salt != nullptr) {
        const char *saltC = env->GetStringUTFChars(salt, nullptr);
        saltedString = std::string(saltC);
        env->ReleaseStringUTFChars(salt, saltC);
    }



    std::string binary = base64Decode(expectedHash);

    if(reverse){
        std::reverse(binary.begin(), binary.end());
    }


    std::string expectedHashXorDeobfuscate = xorObfuscate(binary, static_cast<char>(xor_key));
    std::string expectedHashBase64 = base64Encode(expectedHashXorDeobfuscate);  // ← add this function


    return (appHash == expectedHashBase64) ? JNI_TRUE : JNI_FALSE;
}