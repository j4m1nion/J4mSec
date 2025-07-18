package com.jam1nion.j4msec.features.biometricauth.models

import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.BiometricLockCallback
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

internal object LockState {
    const val TAG = "LockState"
    var locked : AtomicBoolean = AtomicBoolean(false)
        private set

    private var callback : WeakReference<BiometricLockCallback>? = null

    internal fun lock(){
        locked.set(true)
        callback?.get()?.onLocked()
    }

    internal fun unlock(){
        locked.set(false)
        callback?.get()?.onUnlocked()
    }

    internal fun setCallback(callback: BiometricLockCallback?){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "Registered callback: $callback",
                LoggingLevel.INFO
            )
        }
        this.callback = WeakReference(callback)

    }

    internal fun removeCallback(){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "removed callback: $callback",
                LoggingLevel.INFO
            )
        }
        this.callback = null
    }

}