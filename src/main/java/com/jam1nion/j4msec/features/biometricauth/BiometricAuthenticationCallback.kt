package com.jam1nion.j4msec.features.biometricauth

interface BiometricAuthenticationCallback {

    fun onUnlockedSuccess()
    fun onUnlockedCanceled()
    fun onUnlockedNotPossible()

}