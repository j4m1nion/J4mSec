package com.jam1nion.j4msec.features.biometricauth.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManagerImpl
import com.jam1nion.j4msec.features.biometricauth.models.LockState
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel

internal class BiometricAuthenticationLockHostActivity : AppCompatActivity() {

    companion object{
        const val TAG = "BiometricAuthenticationLockHostActivity"
    }

    private val biometricLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == BiometricAuthenticationManagerImpl.BiometricAuthenticationResponse.SUCCESS.resultCode){
            LockState.unlock()
            finish()
        }
        else{
            launchLockActivity()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(J4mSec.biometricAuthenticationManager?.isDeviceSecured(this) == false){
            if(J4mSec.configuration.enableLogging){
                J4mSec.secureLogManager?.logAsync(
                    TAG,
                    "BiometricAuthenticationLockHostActivity finished since device is not secure.",
                    LoggingLevel.WARN
                )
            }
            finish()
        }
        launchLockActivity()
    }

    fun launchLockActivity(){
        biometricLauncher.launch(Intent(this, BiometricAuthenticationLockActivity::class.java))
    }
}