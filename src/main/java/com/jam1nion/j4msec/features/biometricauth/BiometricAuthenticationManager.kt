package com.jam1nion.j4msec.features.biometricauth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.jam1nion.j4msec.features.biometricauth.models.LockStatus
import kotlinx.coroutines.flow.Flow

interface BiometricAuthenticationManager {

    companion object{
        val TAG : String = "BiometricAuthenticationManager"
        val DEFAULT_REQUEST_ID : Int = 100
    }


    fun getLockStatusFLow(): Flow<LockStatus>
    fun getLockStatusLD(): LiveData<LockStatus>
    fun observeLockStatus(owner: LifecycleOwner, handler: (LockStatus) -> Unit)
    fun removeObserver(owner: LifecycleOwner)
    fun isBiometricAvailable(context: Context) : Boolean
    fun isDeviceSecured(context: Context) : Boolean
    fun biometricLock(context: Context, requestId: Int ? = null, callback: BiometricAuthenticationCallback ? = null)
    fun biometricLockBlocking(context: Context, requestId: Int ? = null, callback: BiometricAuthenticationCallback ? = null)
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