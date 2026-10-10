package com.ciclovida.app.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciclovida.app.domain.model.PhaseWellbeingInsight
import com.ciclovida.app.domain.usecase.GenerateHolisticInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(val insights: List<PhaseWellbeingInsight> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val generateHolisticInsights: GenerateHolisticInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val insights = generateHolisticInsights()
            _uiState.value = InsightsUiState(insights = insights, isLoading = false)
        }
    }
}
