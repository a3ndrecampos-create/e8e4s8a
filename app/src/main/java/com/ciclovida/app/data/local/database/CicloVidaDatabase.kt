package com.ciclovida.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ciclovida.app.data.local.dao.*
import com.ciclovida.app.data.local.entity.*

/**
 * Banco 100% local. Nenhum dado de saúde sai deste arquivo SQLite no dispositivo —
 * não há cliente de rede em lugar nenhum do app para enviá-lo a lugar algum.
 */
@Database(
    entities = [
        PeriodDayEntryEntity::class,
        SymptomLogEntity::class,
        DailyWellbeingLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CicloVidaDatabase : RoomDatabase() {
    abstract fun periodDayDao(): PeriodDayDao
    abstract fun symptomLogDao(): SymptomLogDao
    abstract fun wellbeingLogDao(): WellbeingLogDao

    companion object {
        const val DATABASE_NAME = "ciclovida.db"
    }
}
