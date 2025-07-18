package com.jam1nion.j4msec.models

data class J4msecConfiguration(
    val enableLogging: Boolean = true,
    val securePrefsConfiguration: J4msecSecurePrefsConfiguration = J4msecSecurePrefsConfiguration(),
    val secureLogConfiguration: J4msecSecureLogConfiguration = J4msecSecureLogConfiguration()
)