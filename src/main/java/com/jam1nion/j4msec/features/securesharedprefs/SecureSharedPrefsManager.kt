package com.jam1nion.j4msec.features.securesharedprefs

import androidx.lifecycle.LifecycleOwner
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsErrors
import com.jam1nion.j4msec.features.securesharedprefs.models.SecureSharedPrefsKeyHealth
import kotlinx.coroutines.CoroutineScope

interface SecureSharedPrefsManager {

    fun observeErrors(owner: LifecycleOwner, handler: (SecureSharedPrefsErrors) -> Unit)
    fun observeAndAutoReset(owner: LifecycleOwner, coroutineScope: CoroutineScope)
    fun checkKeyHealth() : SecureSharedPrefsKeyHealth
    fun putString(key: String, value : String, commit : Boolean = false)
    fun getString(key: String) : String?
    fun remove(key: String)
    fun clear()
    fun hasKey(key: String) : Boolean
    fun resetDueKeyInvalid()


}