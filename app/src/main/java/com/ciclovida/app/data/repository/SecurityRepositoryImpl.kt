package com.ciclovida.app.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ciclovida.app.domain.repository.SecurityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Por serem dados de saúde reprodutiva (categoria sensível), guardamos o PIN com uma
 * camada extra em relação a um app comum: EncryptedSharedPreferences, que usa o
 * Android Keystore para criptografar a chave mestra — não um DataStore simples.
 * Nada aqui jamais trafega pela rede.
 */
@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityRepository {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "ciclovida_security",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun isPinConfigured(): Boolean = prefs.contains(KEY_PIN_HASH)

    override suspend fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == hash(pin)
    }

    override suspend fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    override suspend fun isDiscreetModeEnabled(): Boolean = prefs.getBoolean(KEY_DISCREET_MODE, false)

    override suspend fun setDiscreetModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DISCREET_MODE, enabled).apply()
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((SALT + value).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_DISCREET_MODE = "discreet_mode"
        private const val SALT = "ciclovida_v1_"
    }
}
