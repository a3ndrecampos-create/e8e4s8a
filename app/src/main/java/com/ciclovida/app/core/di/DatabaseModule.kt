package com.ciclovida.app.core.di

import android.content.Context
import androidx.room.Room
import com.ciclovida.app.data.local.dao.PeriodDayDao
import com.ciclovida.app.data.local.dao.SymptomLogDao
import com.ciclovida.app.data.local.dao.WellbeingLogDao
import com.ciclovida.app.data.local.database.CicloVidaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CicloVidaDatabase =
        Room.databaseBuilder(context, CicloVidaDatabase::class.java, CicloVidaDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePeriodDayDao(db: CicloVidaDatabase): PeriodDayDao = db.periodDayDao()
    @Provides fun provideSymptomLogDao(db: CicloVidaDatabase): SymptomLogDao = db.symptomLogDao()
    @Provides fun provideWellbeingLogDao(db: CicloVidaDatabase): WellbeingLogDao = db.wellbeingLogDao()
}
