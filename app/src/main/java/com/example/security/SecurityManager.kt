package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("orr_expency_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOCK_SECONDS = "auto_lock_seconds"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
    }

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_SECONDS, 60) // default 1 min
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_SECONDS, value).apply()

    private var lastActiveTime: Long
        get() = prefs.getLong(KEY_LAST_ACTIVE_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, value).apply()

    fun setPin(pin: String) {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun disablePin() {
        prefs.edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        val saltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val computedHash = hashPin(inputPin, salt)
        return computedHash == storedHash
    }

    fun recordUserActivity() {
        lastActiveTime = System.currentTimeMillis()
    }

    fun shouldLockOnResume(): Boolean {
        if (!isPinEnabled) return false
        if (autoLockSeconds == -1) return false // Never auto-lock
        if (autoLockSeconds == 0) return true   // Immediately

        val elapsed = (System.currentTimeMillis() - lastActiveTime) / 1000
        return elapsed > autoLockSeconds
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}
