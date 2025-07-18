package com.jam1nion.j4msec.features.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object CryptoUtils {

    private const val ANDROID_KSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "SECURE_PREFS_KEY_J4"
    private const val KEY_STRONG_ALIAS = "SECURE_PREFS_STRONG_KEY_J4"
    private const val KEY_LOGGING_ALIAS = "SECURE_LOGGING_KEY_J4"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    internal fun getPreGenerateSecretKey(): SecretKey? {
        val keystore = KeyStore.getInstance(ANDROID_KSTORE).apply { load(null) }
        return (keystore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }

    internal fun getPreGenerateLoggingSecretKey(): SecretKey? {
        val keystore = KeyStore.getInstance(ANDROID_KSTORE).apply { load(null) }
        return (keystore.getEntry(KEY_LOGGING_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }

    internal fun getPreGenerateStrongSecretKey(): SecretKey? {
        val keystore = KeyStore.getInstance(ANDROID_KSTORE).apply { load(null) }
        return (keystore.getEntry(KEY_STRONG_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }

    internal fun getLoggingSecretKey() : SecretKey {
        return getPreGenerateLoggingSecretKey() ?: generateLoggingKey()
    }


    internal fun getSecretKey() : SecretKey {
        return getPreGenerateSecretKey() ?: generateKey()
    }

    internal  fun getSecretStrongKey(timeout: Int) : SecretKey {
        return getPreGenerateStrongSecretKey() ?: generateStrongKey(timeout)
    }

    internal fun generateLoggingKey() : SecretKey{
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KSTORE)
        val specBuilder = KeyGenParameterSpec.Builder(
            KEY_LOGGING_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)

        keyGen.init(specBuilder.build())
        return keyGen.generateKey()
    }

    internal fun generateKey() : SecretKey{
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KSTORE)
        val specBuilder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)

        keyGen.init(specBuilder.build())
        return keyGen.generateKey()
    }

    internal fun generateStrongKey(timeout: Int) : SecretKey{
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KSTORE)
        val specBuilder = KeyGenParameterSpec.Builder(
            KEY_STRONG_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            specBuilder.setUserAuthenticationParameters(timeout, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
        }
        else{
            specBuilder.setUserAuthenticationValidityDurationSeconds(timeout)
        }

        keyGen.init(specBuilder.build())
        return keyGen.generateKey()
    }


    internal  fun encrypt(key: SecretKey, text : String) : String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        val ivAndCipherText = iv + cipherText
        return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP)
    }

    internal  fun decrypt(key: SecretKey, base64Input: String) : String {
        val ivAndCipherText = Base64.decode(base64Input, Base64.NO_WRAP)
        val iv = ivAndCipherText.copyOfRange(0 , 12)
        val cipherText = ivAndCipherText.sliceArray(12 until ivAndCipherText.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }

    internal  fun removeSecretKey(){
        val keystore = KeyStore.getInstance(ANDROID_KSTORE).apply { load(null) }
        keystore.deleteEntry(KEY_ALIAS)
    }

    internal  fun removeSecretStrongKeys(){
        val keystore = KeyStore.getInstance(ANDROID_KSTORE).apply { load(null) }
        keystore.deleteEntry(KEY_STRONG_ALIAS)
    }

    internal fun computeSha256Base64(file: File, algorithm: String = "SHA-256"): String{
        val digest = MessageDigest.getInstance(algorithm)
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also {
                    read = it
                } > 0){
                digest.update(buffer, 0, read)
            }
        }
        return android.util.Base64.encodeToString(digest.digest(), android.util.Base64.NO_WRAP)
    }

}