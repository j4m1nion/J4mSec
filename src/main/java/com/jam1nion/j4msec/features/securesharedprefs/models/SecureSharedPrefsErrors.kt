package com.jam1nion.j4msec.features.securesharedprefs.models

sealed class SecureSharedPrefsErrors(val motivation: Throwable) : Throwable() {
     class KeyInvalid(motivation: Throwable) : SecureSharedPrefsErrors(motivation)
     class AuthRequired(motivation: Throwable): SecureSharedPrefsErrors(motivation)
     class Unexpected(motivation: Throwable): SecureSharedPrefsErrors(motivation)
}