//
// Created by mamil on 05/07/2025.
//

#ifndef J4MSEC_UTILS_H
#define J4MSEC_UTILS_H

#include <jni.h>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <android/log.h>

std::string toHexString(const jbyte* bytes, jsize length);
jbyteArray jstringToJbyteArray(JNIEnv *env, jstring input);
std::string base64Encode(const std::string& input);
std::string base64Decode(const std::string &encoded);
std::string xorObfuscate(const std::string &input, char key);
std::string xorAndReverse(const std::string& input, char xorKey);

#endif //J4MSEC_UTILS_H
