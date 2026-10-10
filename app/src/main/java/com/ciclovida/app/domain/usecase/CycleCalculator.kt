package com.ciclovida.app.domain.usecase

import com.ciclovida.app.domain.model.Cycle
import com.ciclovida.app.domain.model.CyclePhase
import com.ciclovida.app.domain.model.PeriodDayEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

/**
 * Núcleo matemático do app: transforma os dias de menstruação registrados em
 * ciclos, calcula médias ponderadas (dando mais peso aos ciclos recentes, já que
 * o corpo muda com o tempo) e classifica qualquer data numa fase do ciclo.
 *
 * Importante: isso é um algoritmo estatístico sobre os dados da própria usuária,
 * não um modelo de IA treinado em nuvem — por isso funciona 100% offline e melhora
 * de precisão conforme mais ciclos são registrados, sem nunca enviar nada para fora.
 */
object CycleCalculator {

    private const val DEFAULT_LUTEAL_PHASE_LENGTH = 14
    private const val MAX_CYCLES_CONSIDERED = 6

    /** Agrupa dias de menstruação consecutivos em ciclos (do início de um período ao início do próximo). */
    fun buildCycles(entries: List<PeriodDayEntry>): List<Cycle> {
        if (entries.isEmpty()) return emptyList()
        val sortedDates = entries.map { it.date }.distinct().sorted()

        // Agrupa datas consecutivas (diferença de 1 dia) em "períodos" (sangramentos individuais)
        val periods = mutableListOf<List<LocalDate>>()
        var current = mutableListOf(sortedDates.first())
        for (i in 1 until sortedDates.size) {
            val prev = sortedDates[i - 1]
            val date = sortedDates[i]
            if (ChronoUnit.DAYS.between(prev, date) <= 1) {
                current.add(date)
            } else {
                periods.add(current)
                current = mutableListOf(date)
            }
        }
        periods.add(current)

        return periods.mapIndexed { index, period ->
            val startDate = period.first()
            val nextPeriodStart = periods.getOrNull(index + 1)?.first()
            Cycle(
                startDate = startDate,
                endDate = nextPeriodStart?.minusDays(1),
                lengthInDays = nextPeriodStart?.let { ChronoUnit.DAYS.between(startDate, it).toInt() },
                periodLengthInDays = period.size
            )
        }
    }

    /** Média ponderada do comprimento dos últimos ciclos (peso maior para os mais recentes). */
    fun weightedAverageCycleLength(cycles: List<Cycle>): Double {
        val completed = cycles.mapNotNull { it.lengthInDays }.takeLast(MAX_CYCLES_CONSIDERED)
        if (completed.isEmpty()) return 28.0 // fallback: média populacional, só usado sem nenhum histórico

        var weightedSum = 0.0
        var weightTotal = 0.0
        completed.forEachIndexed { index, length ->
            val weight = (index + 1).toDouble() // ciclos mais recentes (índices maiores) pesam mais
            weightedSum += length * weight
            weightTotal += weight
        }
        return weightedSum / weightTotal
    }

    fun cycleLengthStdDeviation(cycles: List<Cycle>): Double {
        val completed = cycles.mapNotNull { it.lengthInDays }.takeLast(MAX_CYCLES_CONSIDERED)
        if (completed.size < 2) return 0.0
        val mean = completed.average()
        val variance = completed.sumOf { (it - mean) * (it - mean) } / completed.size
        return sqrt(variance)
    }

    fun averagePeriodLength(cycles: List<Cycle>): Int {
        if (cycles.isEmpty()) return 5
        return cycles.takeLast(MAX_CYCLES_CONSIDERED).map { it.periodLengthInDays }.average().let { Math.round(it).toInt() }
    }

    /**
     * Classifica uma data em uma fase do ciclo, com base no ciclo mais recente que a contém
     * (ou, se for uma data futura, projetando a partir do último ciclo conhecido).
     */
    fun classifyPhase(
        date: LocalDate,
        cycles: List<Cycle>,
        averageCycleLength: Double,
        averagePeriodLength: Int,
        lutealPhaseLength: Int = DEFAULT_LUTEAL_PHASE_LENGTH
    ): CyclePhase {
        if (cycles.isEmpty()) return CyclePhase.INDEFINIDA

        // Encontra o ciclo cujo intervalo contém a data, ou o mais próximo anterior.
        val cycle = cycles.lastOrNull { !it.startDate.isAfter(date) } ?: return CyclePhase.INDEFINIDA
        val dayInCycle = ChronoUnit.DAYS.between(cycle.startDate, date).toInt() + 1 // dia 1 = início do período

        val cycleLength = cycle.lengthInDays ?: averageCycleLength.toInt()
        val ovulationDay = (cycleLength - lutealPhaseLength).coerceAtLeast(averagePeriodLength + 1)

        return when {
            dayInCycle in 1..cycle.periodLengthInDays -> CyclePhase.MENSTRUAL
            dayInCycle in (ovulationDay - 1)..(ovulationDay + 1) -> CyclePhase.OVULATORIA
            dayInCycle < ovulationDay -> CyclePhase.FOLICULAR
            dayInCycle > ovulationDay -> CyclePhase.LUTEA
            else -> CyclePhase.INDEFINIDA
        }
    }
}
