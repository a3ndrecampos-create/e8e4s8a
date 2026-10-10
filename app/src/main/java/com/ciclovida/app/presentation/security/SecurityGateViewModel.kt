package com.ciclovida.app.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciclovida.app.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityGateUiState(
    val isPinConfigured: Boolean? = null,
    val isAuthenticated: Boolean = false,
    val biometricAvailable: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SecurityGateViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityGateUiState())
    val uiState: StateFlow<SecurityGateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val configured = securityRepository.isPinConfigured()
            val biometric = securityRepository.isBiometricEnabled()
            _uiState.update { it.copy(isPinConfigured = configured, biometricAvailable = biometric) }
        }
    }

    fun onCreatePin(pin: String, confirmation: String) {
        if (pin.length != 6) {
            _uiState.update { it.copy(errorMessage = "O PIN precisa ter 6 dígitos") }
            return
        }
        if (pin != confirmation) {
            _uiState.update { it.copy(errorMessage = "Os PINs não coincidem") }
            return
        }
        viewModelScope.launch {
            securityRepository.setPin(pin)
            _uiState.update { it.copy(isPinConfigured = true, isAuthenticated = true, errorMessage = null) }
        }
    }

    fun onSubmitPin(pin: String) {
        viewModelScope.launch {
            val valid = securityRepository.verifyPin(pin)
            _uiState.update {
                if (valid) it.copy(isAuthenticated = true, errorMessage = null)
                else it.copy(errorMessage = "PIN incorreto")
            }
        }
    }

    fun onRequestBiometric() {
        // Disparo real do BiometricPrompt fica a cargo da Activity; ver onRequestBiometric na tela.
    }
}
