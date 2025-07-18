package com.jam1nion.j4msec.features.biometricauth

import android.content.Context
import androidx.fragment.app.FragmentActivity

interface BiometricAuthenticationManager {

    fun isBiometricAvailable(context: Context) : Boolean
    fun isDeviceSecured(context: Context) : Boolean
    fun registerCallback(callback: BiometricLockCallback)
    fun removeCallback()
    fun biometricAuth(context: Context)
    fun biometricLock(context: Context)
    fun showPrompt(
        context: Context,
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    )
    fun appLock(context: Context, delay: Long = 3000L, callback: BiometricLockCallback ? = null)
    fun appUnlock()

}