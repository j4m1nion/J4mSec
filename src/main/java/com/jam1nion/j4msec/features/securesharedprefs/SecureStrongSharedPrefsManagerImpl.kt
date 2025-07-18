package com.jam1nion.j4msec.features.securesharedprefs

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.UserNotAuthenticatedException
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jam1nion.j4msec.arch.SingleLiveEvent
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsErrors
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsKeyHealth
import com.jam1nion.j4msec.features.utils.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.InvalidKeyException

internal class SecureStrongSharedPrefsManagerImpl() : SecureStrongSharedPrefsManager {
    companion object{
        private const val TAG = "SecureStrongSharedPrefsManager"
        private const val CHECK_STRONG_SECRET_KEY = "___check_strong_secret_key___"
        private const val CHECK_SECRET_KEY_VALUE = "test_secret_key"

    }
    private var authenticationTimeoutSec : Int = 30

    private var prefs : SharedPreferences ? = null
    private val _errors : MutableLiveData<SecureSharedPrefsErrors> = SingleLiveEvent()
    val errors: LiveData<SecureSharedPrefsErrors> = _errors

    internal fun init(context: Context, name: String, authenticationTimeoutSec: Int = 30){
        this.prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        this.authenticationTimeoutSec = authenticationTimeoutSec
    }


    override fun observeErrors(owner: LifecycleOwner, handler: (SecureSharedPrefsErrors) -> Unit){
        errors.observe(owner) { error -> handler.invoke(error) }
    }

    override fun observeAndAutoReset(owner: LifecycleOwner, coroutineScope: CoroutineScope){
        observeErrors(owner) { error ->
            if(error is SecureSharedPrefsErrors.KeyInvalid){
                coroutineScope.launch {
                    resetDueKeyInvalid()
                }
            }
        }
    }

    override fun checkKeyHealth() : SecureSharedPrefsKeyHealth {
        return try {
            val encrypted = prefs?.getString(CHECK_STRONG_SECRET_KEY, null)
            val key = CryptoUtils.getSecretStrongKey(authenticationTimeoutSec)
            if(encrypted == null){
                prefs?.edit {putString(CHECK_STRONG_SECRET_KEY, CryptoUtils.encrypt(key, CHECK_SECRET_KEY_VALUE))}
            }
            else{
                CryptoUtils.decrypt(key, encrypted)
            }

            SecureSharedPrefsKeyHealth.VALID
        }
        catch (ex : UserNotAuthenticatedException){
            SecureSharedPrefsKeyHealth.NEEDS_AUTH
        }
        catch (ex: InvalidKeyException){
            SecureSharedPrefsKeyHealth.INVALID
        }
    }

    override fun putString(key: String, value : String, commit : Boolean){
        try {
            val encrypted = CryptoUtils.encrypt(CryptoUtils.getSecretStrongKey(authenticationTimeoutSec), value)
            prefs?.edit(commit) { putString(key, encrypted) }
        }
        catch (ex: UserNotAuthenticatedException){
            _errors.postValue(SecureSharedPrefsErrors.AuthRequired(ex))
        }
        catch (ex: InvalidKeyException){
            _errors.postValue(SecureSharedPrefsErrors.KeyInvalid(ex))
        }
        catch (ex : Exception){
            _errors.postValue(SecureSharedPrefsErrors.Unexpected(ex))
        }

    }

    override fun getString(key: String) : String? {
        return try {
            val encrypted = prefs?.getString(key, null) ?: return null
            CryptoUtils.decrypt(CryptoUtils.getSecretStrongKey(authenticationTimeoutSec), encrypted)
        }
        catch (ex: UserNotAuthenticatedException){
            _errors.postValue(SecureSharedPrefsErrors.AuthRequired(ex))
            null
        }
        catch (ex: InvalidKeyException){
            _errors.postValue(SecureSharedPrefsErrors.KeyInvalid(ex))
            null
        }
        catch (ex : Exception){
            _errors.postValue(SecureSharedPrefsErrors.Unexpected(ex))
            null
        }

    }

    override fun remove(key: String) {
        prefs?.edit { remove(key) }
    }

    override fun clear() {
        prefs?.edit { clear() }
    }

    override fun hasKey(key: String) = prefs?.contains(key) == true

    override fun resetDueKeyInvalid(){
        CryptoUtils.removeSecretStrongKeys()
        CryptoUtils.getSecretStrongKey(authenticationTimeoutSec)
        prefs?.edit(commit = true) { clear() }
    }

}