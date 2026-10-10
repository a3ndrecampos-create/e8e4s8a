package com.ciclovida.app.data.repository

import com.ciclovida.app.data.local.dao.PeriodDayDao
import com.ciclovida.app.data.local.dao.SymptomLogDao
import com.ciclovida.app.data.local.dao.WellbeingLogDao
import com.ciclovida.app.data.local.entity.DailyWellbeingLogEntity
import com.ciclovida.app.data.local.entity.PeriodDayEntryEntity
import com.ciclovida.app.data.local.entity.SymptomLogEntity
import com.ciclovida.app.domain.model.*
import com.ciclovida.app.domain.repository.PeriodRepository
import com.ciclovida.app.domain.repository.SymptomRepository
import com.ciclovida.app.domain.repository.WellbeingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class PeriodRepositoryImpl @Inject constructor(
    private val dao: PeriodDayDao
) : PeriodRepository {

    override fun observeAllEntries(): Flow<List<PeriodDayEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<PeriodDayEntry>> =
        dao.observeBetween(start.toEpochDay(), end.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertEntry(entry: PeriodDayEntry) {
        val existing = dao.findByEpochDay(entry.date.toEpochDay())
        dao.upsert(entry.toEntity(existingId = existing?.id ?: 0))
    }

    override suspend fun deleteEntry(date: LocalDate) = dao.deleteByEpochDay(date.toEpochDay())

    override suspend fun getAllEntriesOnce(): List<PeriodDayEntry> = dao.getAllOnce().map { it.toDomain() }
}

class SymptomRepositoryImpl @Inject constructor(
    private val dao: SymptomLogDao
) : SymptomRepository {

    override fun observeSymptomsForDate(date: LocalDate): Flow<List<SymptomLog>> =
        dao.observeForDate(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun setSymptomsForDate(date: LocalDate, symptoms: List<SymptomLog>) {
        // Substitui todos os sintomas do dia pela nova lista (mais simples e previsível pra UI de seleção múltipla)
        dao.deleteForDate(date.toEpochDay())
        if (symptoms.isNotEmpty()) {
            dao.insertAll(symptoms.map { it.toEntity(date) })
        }
    }

    override suspend fun getAllSymptomsOnce(): List<SymptomLog> = dao.getAllOnce().map { it.toDomain() }
}

class WellbeingRepositoryImpl @Inject constructor(
    private val dao: WellbeingLogDao
) : WellbeingRepository {

    override fun observeLogForDate(date: LocalDate): Flow<DailyWellbeingLog?> =
        dao.observeForDate(date.toEpochDay()).map { it?.toDomain() }

    override suspend fun upsertLog(log: DailyWellbeingLog) {
        val existing = dao.findByEpochDay(log.date.toEpochDay())
        dao.upsert(log.toEntity(existingId = existing?.id ?: 0))
    }

    override suspend fun getAllLogsOnce(): List<DailyWellbeingLog> = dao.getAllOnce().map { it.toDomain() }
}

// --- Mappers ---

private fun PeriodDayEntryEntity.toDomain() = PeriodDayEntry(
    id = id, date = LocalDate.ofEpochDay(epochDay), flowIntensity = FlowIntensity.valueOf(flowIntensity)
)

private fun PeriodDayEntry.toEntity(existingId: Long) = PeriodDayEntryEntity(
    id = existingId, epochDay = date.toEpochDay(), flowIntensity = flowIntensity.name
)

private fun SymptomLogEntity.toDomain() = SymptomLog(
    id = id, date = LocalDate.ofEpochDay(epochDay), type = SymptomType.valueOf(type), intensity = intensity
)

private fun SymptomLog.toEntity(date: LocalDate) = SymptomLogEntity(
    epochDay = date.toEpochDay(), type = type.name, intensity = intensity
)

private fun DailyWellbeingLogEntity.toDomain() = DailyWellbeingLog(
    id = id,
    date = LocalDate.ofEpochDay(epochDay),
    moods = moodsCsv.split(",").filter { it.isNotBlank() }.map { Mood.valueOf(it) },
    energyLevel = energyLevel,
    notes = notes
)

private fun DailyWellbeingLog.toEntity(existingId: Long) = DailyWellbeingLogEntity(
    id = existingId,
    epochDay = date.toEpochDay(),
    moodsCsv = moods.joinToString(",") { it.name },
    energyLevel = energyLevel,
    notes = notes
)
