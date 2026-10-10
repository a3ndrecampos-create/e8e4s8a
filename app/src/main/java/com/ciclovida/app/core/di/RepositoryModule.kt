package com.ciclovida.app.core.di

import com.ciclovida.app.data.repository.PeriodRepositoryImpl
import com.ciclovida.app.data.repository.SecurityRepositoryImpl
import com.ciclovida.app.data.repository.SymptomRepositoryImpl
import com.ciclovida.app.data.repository.WellbeingRepositoryImpl
import com.ciclovida.app.domain.repository.PeriodRepository
import com.ciclovida.app.domain.repository.SecurityRepository
import com.ciclovida.app.domain.repository.SymptomRepository
import com.ciclovida.app.domain.repository.WellbeingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPeriodRepository(impl: PeriodRepositoryImpl): PeriodRepository

    @Binds @Singleton
    abstract fun bindSymptomRepository(impl: SymptomRepositoryImpl): SymptomRepository

    @Binds @Singleton
    abstract fun bindWellbeingRepository(impl: WellbeingRepositoryImpl): WellbeingRepository

    @Binds @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository
}
