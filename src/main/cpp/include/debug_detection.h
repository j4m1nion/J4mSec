//
// Created by mamil on 05/07/2025.
//

#ifndef J4MSEC_DEBUG_DETECTION_H
#define J4MSEC_DEBUG_DETECTION_H

#include <jni.h>
#include <fstream>
#include <sstream>



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_tamperdetection_TamperDetectionManager_p(JNIEnv *env, jobject thiz);

#endif //J4MSEC_DEBUG_DETECTION_H
