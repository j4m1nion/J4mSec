package com.jam1nion.j4msec.features.biometricauth.models

import androidx.lifecycle.LiveData
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.arch.SingleLiveEvent
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationCallback
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.Lock

internal object LockState {
    const val TAG = "LockState"
    var locked : AtomicBoolean = AtomicBoolean(false)
        private set

   private var callback : WeakReference<BiometricAuthenticationCallback>? = null

    private val _lockStatus = SingleLiveEvent<LockStatus>()
    val lockStatus : LiveData<LockStatus> = _lockStatus

    internal fun lock(requestId: Int){
        locked.set(true)
        _lockStatus.postValue(LockStatus.Locked(requestId))
    }

    internal fun unlock(requestId: Int){
        locked.set(false)
        _lockStatus.postValue(LockStatus.Unlocked(requestId))
    }

    internal fun deviceUnsecure(requestId: Int){
        locked.set(false)
        _lockStatus.postValue(LockStatus.DeviceUnsecure(requestId))
    }

    internal fun registerCallback(callback : BiometricAuthenticationCallback?){
        this.callback = WeakReference(callback)
    }

    internal fun removeCallback(){
        this.callback = null
    }

    internal fun callbackResultToConsumer(){
        if(locked.get()){
            callback?.get()?.onUnlockedCanceled()
        }
        else{
            callback?.get()?.onUnlockedSuccess()
        }

        removeCallback()
    }
}

