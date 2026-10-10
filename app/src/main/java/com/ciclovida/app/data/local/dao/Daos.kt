package com.ciclovida.app.data.local.dao

import androidx.room.*
import com.ciclovida.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDayDao {
    @Query("SELECT * FROM period_days ORDER BY epochDay")
    fun observeAll(): Flow<List<PeriodDayEntryEntity>>

    @Query("SELECT * FROM period_days WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<PeriodDayEntryEntity>>

    @Query("SELECT * FROM period_days ORDER BY epochDay")
    suspend fun getAllOnce(): List<PeriodDayEntryEntity>

    @Upsert
    suspend fun upsert(entity: PeriodDayEntryEntity)

    @Query("DELETE FROM period_days WHERE epochDay = :epochDay")
    suspend fun deleteByEpochDay(epochDay: Long)

    @Query("SELECT * FROM period_days WHERE epochDay = :epochDay LIMIT 1")
    suspend fun findByEpochDay(epochDay: Long): PeriodDayEntryEntity?
}

@Dao
interface SymptomLogDao {
    @Query("SELECT * FROM symptom_logs WHERE epochDay = :epochDay")
    fun observeForDate(epochDay: Long): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs")
    suspend fun getAllOnce(): List<SymptomLogEntity>

    @Query("DELETE FROM symptom_logs WHERE epochDay = :epochDay")
    suspend fun deleteForDate(epochDay: Long)

    @Insert
    suspend fun insertAll(entities: List<SymptomLogEntity>)
}

@Dao
interface WellbeingLogDao {
    @Query("SELECT * FROM wellbeing_logs WHERE epochDay = :epochDay LIMIT 1")
    fun observeForDate(epochDay: Long): Flow<DailyWellbeingLogEntity?>

    @Query("SELECT * FROM wellbeing_logs")
    suspend fun getAllOnce(): List<DailyWellbeingLogEntity>

    @Upsert
    suspend fun upsert(entity: DailyWellbeingLogEntity)

    @Query("SELECT * FROM wellbeing_logs WHERE epochDay = :epochDay LIMIT 1")
    suspend fun findByEpochDay(epochDay: Long): DailyWellbeingLogEntity?
}
