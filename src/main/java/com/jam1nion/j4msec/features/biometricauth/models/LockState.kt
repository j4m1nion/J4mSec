package com.jam1nion.j4msec.features.biometricauth.models

import androidx.lifecycle.LiveData
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.arch.SingleLiveEvent
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock

internal object LockState {
    const val TAG = "LockState"
    var locked : AtomicBoolean = AtomicBoolean(false)
        private set

    private val _lockStatus = SingleLiveEvent<LockStatus>()
    val lockStatus : LiveData<LockStatus> = _lockStatus

    internal fun lock(){
        locked.set(true)
        _lockStatus.postValue(LockStatus.Locked)
    }

    internal fun unlock(){
        locked.set(false)
        _lockStatus.postValue(LockStatus.Unlocked)
    }

    internal fun deviceUnsecure(){
        locked.set(false)
        _lockStatus.postValue(LockStatus.DeviceUnsecure)
    }

}