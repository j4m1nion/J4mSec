package com.jam1nion.j4msec.features.biometricauth.observers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManager
import com.jam1nion.j4msec.features.biometricauth.models.LockState
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel

internal class BiometricAuthenticationLockObserver(
    val lockDelay : Long = 3000L,
    val lockFunction: () -> Unit
) : DefaultLifecycleObserver {

    companion object{
        const val TAG = "BiometricAuthenticationLockObserver"
        const val LOCK_OBSERVER_REQUEST_ID = 222
    }

    private var lockTime : Long = 0L


    fun startObserving() {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "start observing.",
                LoggingLevel.INFO
            )
        }
        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(this)
    }

    fun stopObserving(){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "stop observing.",
                LoggingLevel.INFO
            )
        }
        ProcessLifecycleOwner.Companion.get().lifecycle.removeObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onCreate.",
                LoggingLevel.INFO
            )
        }
        super.onCreate(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onPause.",
                LoggingLevel.INFO
            )
        }
        super.onPause(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onStart.",
                LoggingLevel.INFO
            )
        }
        super.onStart(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onDestroy.",
                LoggingLevel.INFO
            )
        }
        super.onDestroy(owner)
        stopObserving()
    }


    override fun onResume(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onResume.",
                LoggingLevel.INFO
            )
        }
        super.onResume(owner)
        val now = System.currentTimeMillis()
        if(LockState.locked.get() && now - lockTime > lockDelay){
            lockFunction.invoke()
        }

    }


    override fun onStop(owner: LifecycleOwner) {
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "app onStop.",
                LoggingLevel.INFO
            )
        }
        super.onStop(owner)
        lockTime = System.currentTimeMillis()
        LockState.lock(LOCK_OBSERVER_REQUEST_ID)
    }
}