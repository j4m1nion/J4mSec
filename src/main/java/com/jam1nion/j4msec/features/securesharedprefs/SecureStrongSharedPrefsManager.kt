package com.jam1nion.j4msec.features.securesharedprefs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsErrors
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsKeyHealth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SecureStrongSharedPrefsManager {

    fun getErrorsLD() : LiveData<SecureSharedPrefsErrors>
    fun getErrorsFlow() : Flow<SecureSharedPrefsErrors>
    fun observeErrors(owner: LifecycleOwner, handler: (SecureSharedPrefsErrors) -> Unit)
    fun observeAndAutoReset(owner: LifecycleOwner, coroutineScope: CoroutineScope)
    fun removeObservers(owner: LifecycleOwner)
    fun checkKeyHealth() : SecureSharedPrefsKeyHealth
    fun putString(key: String, value : String, commit : Boolean = false)
    fun getString(key: String) : String?
    fun remove(key: String)
    fun clear()
    fun hasKey(key: String) : Boolean
    fun resetDueKeyInvalid()
}