package com.jam1nion.j4msec.features.biometricauth.activities

import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jam1nion.j4msec.databinding.ActivityBiometricAuthenticationLockBinding
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.biometricauth.BiometricAuthenticationManagerImpl
import com.jam1nion.j4msec.features.biometricauth.models.LockState

internal class BiometricAuthenticationLockActivity : AppCompatActivity() {

    private var binding : ActivityBiometricAuthenticationLockBinding ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBiometricAuthenticationLockBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupRetryButton()
        showBiometricPrompt()
    }

    override fun onBackPressed() {
        setResult(BiometricAuthenticationManagerImpl.BiometricAuthenticationResponse.FAIL.resultCode)
        super.onBackPressed()
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()
    }


    private fun showBiometricPrompt() {
        J4mSec.biometricAuthenticationManager?.showPrompt(
                context = this,
                fragmentActivity = this,
                onSuccess = {
                    setResult(BiometricAuthenticationManagerImpl.BiometricAuthenticationResponse.SUCCESS.resultCode)
                    finish()
                },
                onFailure = { showRetry() },
                onError = {  showRetry() }
            )

    }

    private fun setupRetryButton(){
        binding?.biometricRetryButton?.setOnClickListener {
            binding?.biometricRetryButton?.isVisible = false
            showBiometricPrompt()
        }
    }

    private fun showRetry(){
        binding?.biometricRetryButton?.isVisible = true
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}