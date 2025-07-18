package com.jam1nion.j4msec.features.biometricauth

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.jam1nion.j4msec.R
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.activities.BiometricAuthenticationLockActivity
import com.jam1nion.j4msec.features.biometricauth.activities.BiometricAuthenticationLockHostActivity
import com.jam1nion.j4msec.features.biometricauth.models.LockState
import com.jam1nion.j4msec.features.biometricauth.observers.BiometricAuthenticationLockObserver
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel

internal class BiometricAuthenticationManagerImpl : BiometricAuthenticationManager {

    companion object{
        private const val TAG = "BiometricAuthenticationManager"
    }


    enum class BiometricAuthenticationResponse(val resultCode: Int){
        SUCCESS(901),
        FAIL(999),
        ERROR(942)
    }

    private var lockObserver : BiometricAuthenticationLockObserver? = null


    override fun isBiometricAvailable(context: Context) : Boolean{
        val biometricManager = BiometricManager.from(context)
        val biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
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
                TAG,
                "Device is secure: $deviceSecure",
                LoggingLevel.SECURITY
            )
        }

        return deviceSecure
    }

    override fun registerCallback(callback: BiometricLockCallback){
        LockState.setCallback(callback)
    }

    override fun removeCallback(){
        LockState.removeCallback()
    }

    override fun biometricAuth(context: Context){
        val deviceSecure = isDeviceSecured(context)
        if(deviceSecure){

            Intent(context, BiometricAuthenticationLockActivity::class.java).run {
                context.startActivity(this)
            }
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(deviceSecure) "starting BiometricAuthenticationLockActivity" else "biometricAuth ignored since device is not secure.",
                if(deviceSecure) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }

    }

    override  fun biometricLock(context: Context){
        val deviceSecure = isDeviceSecured(context)

        if(deviceSecure) {
            Intent(
                context.applicationContext,
                BiometricAuthenticationLockHostActivity::class.java
            ).run {
                context.startActivity(this)
            }
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(deviceSecure) "starting BiometricAuthenticationLockActivity" else "biometricAuth ignored since device is not secure.",
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


    override fun appLock(context: Context, delay: Long, callback: BiometricLockCallback ?){
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                "appLock requested with callback: $callback",
                LoggingLevel.INFO
            )
        }


        LockState.setCallback(callback)

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
                TAG,
                "appUnlock requested.",
                LoggingLevel.INFO
            )
        }

        lockObserver?.stopObserving()
        lockObserver = null
    }



}