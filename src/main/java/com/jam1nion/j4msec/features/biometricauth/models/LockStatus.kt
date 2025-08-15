package com.jam1nion.j4msec.features.biometricauth.models

sealed class LockStatus(val requestId : Int) {

    class Locked(requestId: Int) : LockStatus(requestId)
    class Unlocked(requestId: Int)  : LockStatus(requestId)
    class DeviceUnsecure(requestId: Int) : LockStatus(requestId)

}