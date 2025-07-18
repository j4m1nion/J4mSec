package com.jam1nion.j4msec.features.appsignatureverification

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

internal class AppSignatureManagerImpl : AppSignatureManager {

    companion object{
        private const val TAG = "AppSignatureManager"
        private const val ALGORITHM = "SHA-256"
        private const val CERTIFICATE_TYPE = "X.509"

    }

    private external fun verifyAppSignatureAsByteArray(certificateHash: ByteArray, appHash : String) : Boolean
    private external fun verifyAppSignatureAsString(certificateHash: String, appHash : String) : Boolean
    private external fun verifyAppSignatureAsStringWithXorAndSalt(certificateHash: String, xorKey: Char, reverse: Boolean, salt: String?, appHash : String) : Boolean

    private fun getSignatureHash(context: Context) : String?{
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )



        return packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()?.let { signature ->
            val certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE)
            val cert = certFactory.generateCertificate(ByteArrayInputStream(signature.toByteArray())) as X509Certificate
            val digest = MessageDigest.getInstance(ALGORITHM)
            val hash = digest.digest(cert.publicKey.encoded)
            Base64.encodeToString(hash, Base64.NO_WRAP)
        }

    }


    override fun appSignature(context: Context, certificateSignature: ByteArray, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit){
        val appHash = getSignatureHash(context)
        val verification = verifyAppSignatureAsByteArray(
            certificateHash = certificateSignature,
            appHash = appHash ?: "")
        if(verification){
            onAppSignatureValid.invoke()
        }
        else{
            onAppSignatureInvalid.invoke()
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }
    }

    override fun appSignature(context: Context, certificateSignature: String, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit){
        val appHash = getSignatureHash(context)
        val verification = verifyAppSignatureAsString(
            certificateHash = certificateSignature,
            appHash = appHash ?: "")
        if(verification){
            onAppSignatureValid.invoke()
        }
        else{
            onAppSignatureInvalid.invoke()
        }

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }

    }

    override fun appSignature(context: Context, certificateSignature: String, xorKey: Char, reverse: Boolean, salt: String?, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit){
        val appHash = getSignatureHash(context)
        val verification = verifyAppSignatureAsStringWithXorAndSalt(
            certificateHash = certificateSignature,
            xorKey = xorKey,
            reverse = reverse,
            salt = salt,
            appHash = appHash ?: "")
        return if(verification){
            onAppSignatureValid.invoke()
        }
        else{
            onAppSignatureInvalid.invoke()
        }
        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.INFO else LoggingLevel.SECURITY
            )
        }

    }

    override fun appSignature(context: Context, certificateSignature: ByteArray) : Boolean{
        val appHash = getSignatureHash(context)
        val verification = verifyAppSignatureAsByteArray(
            certificateHash = certificateSignature,
            appHash = appHash ?: "")

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
        return verification
    }

    override  fun appSignature(context: Context, certificateSignature: String) : Boolean
    {
        val appHash = getSignatureHash(context)
        val verification =  verifyAppSignatureAsString(
            certificateHash = certificateSignature,
            appHash = appHash ?: "")

        if (J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
        return verification
    }

    override  fun appSignature(context: Context, certificateSignature: String, xorKey: Char, reverse: Boolean, salt: String?) : Boolean{
        val appHash = getSignatureHash(context)
        val verification =  verifyAppSignatureAsStringWithXorAndSalt(
            certificateHash = certificateSignature,
            xorKey = xorKey,
            reverse = reverse,
            salt = salt,
            appHash = appHash ?: "")


        if (J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                (if(verification) "App signature integrity succeeded." else "App signature integrity failed.") + " AppHash: $appHash and certificateHash: $certificateSignature",
                if(verification) LoggingLevel.SECURITY else LoggingLevel.INFO
            )
        }
        return verification
    }

}