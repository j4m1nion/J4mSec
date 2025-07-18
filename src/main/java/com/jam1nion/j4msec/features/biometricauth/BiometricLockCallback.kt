package com.jam1nion.j4msec.features.biometricauth

interface BiometricLockCallback {

    fun onLocked()
    fun onUnlocked()
}