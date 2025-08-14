package com.jam1nion.j4msec.features.biometricauth.models

sealed class LockStatus {

    data object Locked : LockStatus()
    data object Unlocked : LockStatus()
    data object DeviceUnsecure: LockStatus()

}