//
// Created by mamil on 05/07/2025.
//

#include "utils.h"


std::string toHexString(const jbyte* bytes, jsize length) {
    std::ostringstream oss;
    for (int i = 0; i < length; ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0') << (bytes[i] & 0xFF);
    }
    return oss.str();
}

jbyteArray jstringToJbyteArray(JNIEnv *env, jstring input) {
    if (input == nullptr) return nullptr;

    const char* utfChars = env->GetStringUTFChars(input, nullptr);
    jsize length = static_cast<jsize>(strlen(utfChars));

    jbyteArray result = env->NewByteArray(length);
    env->SetByteArrayRegion(result, 0, length, reinterpret_cast<const jbyte*>(utfChars));

    env->ReleaseStringUTFChars(input, utfChars);
    return result;
}

std::string base64Encode(const std::string& input) {
    static const char table[] =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    std::string encoded;
    int val = 0, valb = -6;
    for (uint8_t c : input) {
        val = (val << 8) + c;
        valb += 8;
        while (valb >= 0) {
            encoded.push_back(table[(val >> valb) & 0x3F]);
            valb -= 6;
        }
    }
    if (valb > -6)
        encoded.push_back(table[((val << 8) >> (valb + 8)) & 0x3F]);
    while (encoded.size() % 4)
        encoded.push_back('=');
    return encoded;
}

std::string base64Decode(const std::string &encoded) {
    static const std::string base64_chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    std::vector<unsigned char> decoded;
    int val = 0, valb = -8;

    for (unsigned char c : encoded) {
        if (isspace(c)) continue;
        size_t pos = base64_chars.find(c);
        if (pos == std::string::npos) break;

        val = (val << 6) + pos;
        valb += 6;

        if (valb >= 0) {
            decoded.push_back((val >> valb) & 0xFF);
            valb -= 8;
        }
    }

    return {decoded.begin(), decoded.end()};
}

std::string xorObfuscate(const std::string &input, char key){
    std::string result = input;
    for(char &c : result){
        c ^= key;
    }
    return result;
}

std::string xorAndReverse(const std::string& input, char xorKey) {
    std::string xored;
    for (char c : input) {
        xored += c ^ xorKey;
    }
    return std::string(xored.rbegin(), xored.rend());
}