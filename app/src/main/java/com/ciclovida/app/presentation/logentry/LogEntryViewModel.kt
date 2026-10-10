package com.ciclovida.app.presentation.logentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciclovida.app.domain.model.*
import com.ciclovida.app.domain.repository.PeriodRepository
import com.ciclovida.app.domain.repository.SymptomRepository
import com.ciclovida.app.domain.repository.WellbeingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LogEntryUiState(
    val date: LocalDate = LocalDate.now(),
    val flowIntensity: FlowIntensity? = null,
    val selectedSymptoms: Set<SymptomType> = emptySet(),
    val selectedMoods: Set<Mood> = emptySet(),
    val energyLevel: Int = 3,
    val notes: String = "",
    val savedConfirmation: Boolean = false
)

@HiltViewModel
class LogEntryViewModel @Inject constructor(
    private val periodRepository: PeriodRepository,
    private val symptomRepository: SymptomRepository,
    private val wellbeingRepository: WellbeingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogEntryUiState())
    val uiState: StateFlow<LogEntryUiState> = _uiState.asStateFlow()

    init {
        loadForDate(LocalDate.now())
    }

    private fun loadForDate(date: LocalDate) {
        viewModelScope.launch {
            symptomRepository.observeSymptomsForDate(date).collectLatest { symptoms ->
                _uiState.update { it.copy(selectedSymptoms = symptoms.map { s -> s.type }.toSet()) }
            }
        }
        viewModelScope.launch {
            wellbeingRepository.observeLogForDate(date).collectLatest { log ->
                if (log != null) {
                    _uiState.update {
                        it.copy(
                            selectedMoods = log.moods.toSet(),
                            energyLevel = log.energyLevel,
                            notes = log.notes ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onFlowSelected(flow: FlowIntensity?) {
        _uiState.update { it.copy(flowIntensity = flow) }
    }

    fun onToggleSymptom(symptom: SymptomType) {
        _uiState.update {
            val updated = if (symptom in it.selectedSymptoms) it.selectedSymptoms - symptom else it.selectedSymptoms + symptom
            it.copy(selectedSymptoms = updated)
        }
    }

    fun onToggleMood(mood: Mood) {
        _uiState.update {
            val updated = if (mood in it.selectedMoods) it.selectedMoods - mood else it.selectedMoods + mood
            it.copy(selectedMoods = updated)
        }
    }

    fun onEnergyChanged(level: Int) {
        _uiState.update { it.copy(energyLevel = level) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onSave() {
        val state = _uiState.value
        viewModelScope.launch {
            state.flowIntensity?.let { flow ->
                periodRepository.upsertEntry(PeriodDayEntry(date = state.date, flowIntensity = flow))
            }
            symptomRepository.setSymptomsForDate(
                state.date,
                state.selectedSymptoms.map { SymptomLog(date = state.date, type = it, intensity = 2) }
            )
            wellbeingRepository.upsertLog(
                DailyWellbeingLog(
                    date = state.date,
                    moods = state.selectedMoods.toList(),
                    energyLevel = state.energyLevel,
                    notes = state.notes.ifBlank { null }
                )
            )
            _uiState.update { it.copy(savedConfirmation = true) }
        }
    }

    fun onConfirmationShown() {
        _uiState.update { it.copy(savedConfirmation = false) }
    }
}
