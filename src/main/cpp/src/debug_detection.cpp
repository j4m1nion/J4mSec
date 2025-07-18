//
// Created by mamil on 05/07/2025.
//
#include "debug_detection.h"
#include "utils.h"

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_jam1nion_j4msec_features_debugmodedetection_DebugDetectionManagerImpl_p(JNIEnv *env, jobject thiz) {
    std::string p = ")/.;.)u<6?)u95(*u";
    std::string t = "`>3\n(?9;(\x0E";

    std::reverse(p.begin(), p.end());
    std::string por = xorObfuscate(p, 0x5A);

    std::reverse(t.begin(), t.end());
    std::string tor = xorObfuscate(t, 0x5A);

    if(por.empty()){
        return JNI_FALSE;
    }

    std::ifstream statusFile(por);
    std::string line;

    if(!statusFile.is_open()){
        return JNI_FALSE;
    }

    while(std::getline(statusFile, line)){
        if(line.rfind(tor, 0) == 0){
            std::istringstream iss(line.substr(10));
            int tp = 0;
            iss >> tp;
            return tp != 0 ? JNI_TRUE : JNI_FALSE;
        }
    }

    return JNI_FALSE;

}
