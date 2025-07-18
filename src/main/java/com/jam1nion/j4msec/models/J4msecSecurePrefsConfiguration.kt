package com.jam1nion.j4msec.models

data class J4msecSecurePrefsConfiguration(
    val preferenceName: String = "default",
    val strongAuthTimeoutSec: Int = 30
)