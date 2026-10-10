package com.ciclovida.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciclovida.app.domain.model.CyclePhase
import com.ciclovida.app.domain.model.CyclePrediction
import com.ciclovida.app.domain.repository.PeriodRepository
import com.ciclovida.app.domain.usecase.CycleCalculator
import com.ciclovida.app.domain.usecase.PredictNextCycleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val currentPhase: CyclePhase = CyclePhase.INDEFINIDA,
    val prediction: CyclePrediction? = null,
    val daysUntilNextPeriod: Long? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val periodRepository: PeriodRepository,
    private val predictNextCycle: PredictNextCycleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            periodRepository.observeAllEntries().collectLatest { entries ->
                val cycles = CycleCalculator.buildCycles(entries)
                val avgLength = CycleCalculator.weightedAverageCycleLength(cycles)
                val avgPeriodLength = CycleCalculator.averagePeriodLength(cycles)
                val today = LocalDate.now()

                val phase = CycleCalculator.classifyPhase(today, cycles, avgLength, avgPeriodLength)
                val prediction = predictNextCycle()
                val daysUntil = prediction?.let { ChronoUnit.DAYS.between(today, it.nextPeriodStartEstimate) }

                _uiState.value = HomeUiState(
                    today = today,
                    currentPhase = phase,
                    prediction = prediction,
                    daysUntilNextPeriod = daysUntil,
                    isLoading = false
                )
            }
        }
    }
}
