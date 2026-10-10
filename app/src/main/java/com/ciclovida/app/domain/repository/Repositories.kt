package com.ciclovida.app.domain.repository

import com.ciclovida.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PeriodRepository {
    fun observeAllEntries(): Flow<List<PeriodDayEntry>>
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<PeriodDayEntry>>
    suspend fun upsertEntry(entry: PeriodDayEntry)
    suspend fun deleteEntry(date: LocalDate)
    suspend fun getAllEntriesOnce(): List<PeriodDayEntry>
}

interface SymptomRepository {
    fun observeSymptomsForDate(date: LocalDate): Flow<List<SymptomLog>>
    suspend fun setSymptomsForDate(date: LocalDate, symptoms: List<SymptomLog>)
    suspend fun getAllSymptomsOnce(): List<SymptomLog>
}

interface WellbeingRepository {
    fun observeLogForDate(date: LocalDate): Flow<DailyWellbeingLog?>
    suspend fun upsertLog(log: DailyWellbeingLog)
    suspend fun getAllLogsOnce(): List<DailyWellbeingLog>
}

interface SecurityRepository {
    suspend fun isPinConfigured(): Boolean
    suspend fun setPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun isBiometricEnabled(): Boolean
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun isDiscreetModeEnabled(): Boolean
    suspend fun setDiscreetModeEnabled(enabled: Boolean)
}
