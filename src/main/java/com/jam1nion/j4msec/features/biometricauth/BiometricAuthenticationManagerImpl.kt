package com.jam1nion.j4msec.features.biometricauth

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.jam1nion.j4msec.R
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManager.Companion.DEFAULT_REQUEST_ID
import com.jam1nion.j4msec.features.biometricauth.activities.BiometricAuthenticationLockHostActivity
import com.jam1nion.j4msec.features.biometricauth.activities.BiometricAuthenticationLockHostActivity.Companion.ONE_SHOT_ARGUMENT
import com.jam1nion.j4msec.features.biometricauth.activities.BiometricAuthenticationLockHostActivity.Companion.REQUEST_ID
import com.jam1nion.j4msec.features.biometricauth.models.LockState
import com.jam1nion.j4msec.features.biometricauth.models.LockStatus
import com.jam1nion.j4msec.features.biometricauth.observers.BiometricAuthenticationLockObserver
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import kotlinx.coroutines.flow.Flow

internal class BiometricAuthenticationManagerImpl : BiometricAuthenticationManager {


    enum class BiometricAuthenticationResponse(val resultCode: Int){
        SUCCESS(901),
        FAIL(999),
        ERROR(942)
    }

    private var lockObserver : BiometricAuthenticationLockObserver? = null


    override fun getLockStatusFLow(): Flow<LockStatus> {
        return LockState.lockStatus.asFlow()
    }

    override fun getLockStatusLD(): LiveData<LockStatus> {
        return LockState.lockStatus
    }

    override fun observeLockStatus(owner: LifecycleOwner, handler: (LockStatus) -> Unit) {
        LockState.lockStatus.observe(owner) { status -> handler.invoke(status) }
    }

    override fun removeObserver(owner: LifecycleOwner) {
        LockState.lockStatus.removeObservers(owner)
    }

    override fun isBiometricAvailable(context: Context) : Boolean{
        val biometricManager = BiometricManager.from(context)
        val biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                "Biometric login is available: $biometricAvailable",
                LoggingLevel.SECURITY
            )
        }

        return biometricAvailable
    }

    override fun isDeviceSecured(context: Context) : Boolean{
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val deviceSecure =  keyguardManager.isDeviceSecure

        if (J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                "Device is secure: $deviceSecure",
                LoggingLevel.SECURITY
            )
        }

        return deviceSecure
    }


    override fun biometricLockBlocking(context: Context, requestId: Int ?, callback: BiometricAuthenticationCallback ?) {
        val deviceSecure = isDeviceSecured(context)

        if(deviceSecure) {
            LockState.registerCallback(callback)
            Intent(
                context.applicationContext,
                BiometricAuthenticationLockHostActivity::class.java
            ).run {
                putExtras(Bundle().apply { putInt(REQUEST_ID, requestId ?: DEFAULT_REQUEST_ID)})
                context.startActivity(this)
            }
        }
        else{
            callback?.onUnlockedNotPossible()
            LockState.deviceUnsecure(requestId ?: DEFAULT_REQUEST_ID)
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                if(deviceSecure) "biometricLock starting BiometricAuthenticationLockActivity" else "biometricLock ignored since device is not secure.",
                if(deviceSecure) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }
    }

    override  fun biometricLock(context: Context, requestId: Int ?, callback: BiometricAuthenticationCallback ?){
        val deviceSecure = isDeviceSecured(context)

        if(deviceSecure) {
            LockState.registerCallback(callback)
            Intent(
                context.applicationContext,
                BiometricAuthenticationLockHostActivity::class.java
            ).run {
                putExtras(Bundle().apply { putBoolean(ONE_SHOT_ARGUMENT, true)})
                putExtras(Bundle().apply { putInt(REQUEST_ID, requestId ?: DEFAULT_REQUEST_ID)})
                context.startActivity(this)
            }
        }
        else{
            callback?.onUnlockedNotPossible()
            LockState.deviceUnsecure(requestId ?: DEFAULT_REQUEST_ID)
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                if(deviceSecure) "biometricLockBlocking starting BiometricAuthenticationLockActivity" else "biometricLockBlocking ignored since device is not secure.",
                if(deviceSecure) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }

    }



    private fun buildPrompt(context: Context) = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.auth_biometric_title))
        .setSubtitle(context.getString(R.string.auth_biometric_subtitle))
        .setConfirmationRequired(false)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    override fun showPrompt(
        context: Context,
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    ){
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = buildPrompt(context)

        val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError.invoke("$errorCode - $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess.invoke()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure.invoke()
                }
            })
        biometricPrompt.authenticate(prompt)
    }


    override fun appLock(context: Context, delay: Long){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                "appLock requested",
                LoggingLevel.INFO
            )
        }

        if(lockObserver == null){
            lockObserver = BiometricAuthenticationLockObserver(
                lockDelay = delay,
                lockFunction = {
                    biometricLock(context)
                }).also {
                    it.startObserving()
            }
        }


    }

    override fun appUnlock(){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                BiometricAuthenticationManager.TAG,
                "appUnlock requested.",
                LoggingLevel.INFO
            )
        }

        lockObserver?.stopObserving()
        lockObserver = null
    }



}