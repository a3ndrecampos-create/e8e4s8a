package com.ciclovida.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_days")
data class PeriodDayEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val flowIntensity: String
)

@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val type: String,
    val intensity: Int
)

@Entity(tableName = "wellbeing_logs")
data class DailyWellbeingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    /** Moods separados por vírgula (ex.: "FELIZ,ENERGICA") — simples e suficiente para uma lista curta de enums. */
    val moodsCsv: String,
    val energyLevel: Int,
    val notes: String?
)
