package com.applicaster.cleeng.utils

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Base64
import android.util.Log
import com.applicaster.cleeng.BuildConfig
import java.lang.Exception
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import com.applicaster.app.CustomApplication
import java.util.*


@SuppressLint("HardwareIds")
class CryptoUtil {
    private val TAG = CryptoUtil::class.java.canonicalName
    private var PACKAGE_NAME = BuildConfig.APPLICATION_ID
    private val KEYLEN_BITS = 128
    private val IV_BITS_MIN = 12
    private val IV_BITS_MAX = 16
    private val ITERATIONS = 64
    private val skfAlgorithm = "PBEWithMD5AndDES"
    private val transformation = "PBEWithMD5AndDES"

    // lazy generate and get instanceId string
    private val instanceId: String by lazy {
        val id =
            Settings.Secure.getString(CustomApplication.getApplication().contentResolver, Settings.Secure.ANDROID_ID)
        UUID.nameUUIDFromBytes(id.toByteArray()).toString()
    }

    fun encode(value: String): String {
        return Base64.encodeToString(value.toByteArray(), Base64.DEFAULT).trim()
    }

    fun decode(value: String): String {
        return String(Base64.decode(value, Base64.DEFAULT)).trim()
    }

    private fun encodeToken(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT).trim()
    }

    private fun decodeToken(data: String): ByteArray {
        return Base64.decode(data, Base64.DEFAULT)
    }

    fun encryptToken(token: String): String {
        val key = generateSecretKey(instanceId)
        val encryptedData = encrypt(key, token)
        return encodeToken(encryptedData)
    }

    fun decryptToken(token: String): String {
        val key = generateSecretKey(instanceId)
        val decodedToken = decodeToken(token)
        var decryptedData = ""
        try {
            decryptedData = decrypt(key, decodedToken)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return decryptedData
    }

    private fun generateSecretKey(passphrase: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(skfAlgorithm)
        val spec = PBEKeySpec(
            passphrase.toCharArray(),
            (instanceId + PACKAGE_NAME).toByteArray(Charsets.UTF_8),
            ITERATIONS,
            KEYLEN_BITS
        )
        return factory.generateSecret(spec)
    }

    private fun encrypt(secretKey: SecretKey, token: String): ByteArray {
        var byteBuffer: ByteBuffer? = null
        try {
            val iv = ByteArray(IV_BITS_MIN)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(iv)
            val cipher = Cipher.getInstance(transformation)
            val parameterSpec =
                PBEParameterSpec((instanceId + PACKAGE_NAME).toByteArray(Charsets.UTF_8), ITERATIONS)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
            val cipherBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            byteBuffer = ByteBuffer.allocate(4 + iv.size + cipherBytes.size)
            byteBuffer.putInt(iv.size)
            byteBuffer.put(iv)
            byteBuffer.put(cipherBytes)
        } catch (e: IllegalStateException) {
            Log.e(TAG, e.message)
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, e.message)
        } catch (e: BadPaddingException) {
            Log.e(TAG, e.message)
        } catch (e: BufferUnderflowException) {
            Log.e(TAG, e.message)
        }
        return byteBuffer?.array() ?: byteArrayOf()
    }

    private fun decrypt(secretKey: SecretKey, token: ByteArray): String {
        val ivLength: Int
        val iv: ByteArray
        val cipherBytes: ByteArray
        var encryptedBytes: ByteArray? = null
        try {
            val buffer = ByteBuffer.wrap(token)
            ivLength = buffer.int
            if (ivLength < IV_BITS_MIN || ivLength >= IV_BITS_MAX) {
                Log.e(TAG, "Failed to obtain IV data!")
            }
            iv = ByteArray(ivLength)
            buffer.get(iv)
            cipherBytes = ByteArray(buffer.remaining())
            buffer.get(cipherBytes)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                PBEParameterSpec((instanceId + PACKAGE_NAME).toByteArray(Charsets.UTF_8), ITERATIONS)
            )
            encryptedBytes = cipher.doFinal(cipherBytes)
        } catch (e: IllegalStateException) {
            Log.e(TAG, e.message)
        } catch (e: IllegalBlockSizeException) {
            Log.e(TAG, e.message)
        } catch (e: BadPaddingException) {
            Log.e(TAG, e.message)
        } catch (e: BufferUnderflowException) {
            Log.e(TAG, e.message)
        }
        return String(encryptedBytes ?: byteArrayOf(), Charsets.UTF_8)
    }
}