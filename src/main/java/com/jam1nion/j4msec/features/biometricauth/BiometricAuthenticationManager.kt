package com.jam1nion.j4msec.features.biometricauth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.jam1nion.j4msec.features.biometricauth.models.LockStatus

interface BiometricAuthenticationManager {

    fun observeLockStatus(owner: LifecycleOwner, handler: (LockStatus) -> Unit)
    fun isBiometricAvailable(context: Context) : Boolean
    fun isDeviceSecured(context: Context) : Boolean
    fun biometricLock(context: Context)
    fun biometricLockBlocking(context: Context)
    fun showPrompt(
        context: Context,
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    )
    fun appLock(context: Context, delay: Long = 3000L)
    fun appUnlock()

}