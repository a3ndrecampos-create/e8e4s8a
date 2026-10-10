package com.ciclovida.app.domain.usecase

import com.ciclovida.app.domain.model.*
import com.ciclovida.app.domain.repository.PeriodRepository
import com.ciclovida.app.domain.repository.SymptomRepository
import com.ciclovida.app.domain.repository.WellbeingRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * Calcula a previsão da próxima menstruação, janela fértil e ovulação com base
 * no histórico real de ciclos da usuária. Essa é a funcionalidade "preditiva"
 * central do app: um algoritmo estatístico adaptativo, não um valor fixo de 28 dias.
 */
class PredictNextCycleUseCase @Inject constructor(
    private val periodRepository: PeriodRepository
) {
    suspend operator fun invoke(): CyclePrediction? {
        val entries = periodRepository.getAllEntriesOnce()
        val cycles = CycleCalculator.buildCycles(entries)
        val lastCycle = cycles.lastOrNull() ?: return null

        val completedCycles = cycles.count { it.lengthInDays != null }
        val avgLength = CycleCalculator.weightedAverageCycleLength(cycles)
        val stdDev = CycleCalculator.cycleLengthStdDeviation(cycles)
        val avgPeriodLength = CycleCalculator.averagePeriodLength(cycles)

        val nextPeriodStart = lastCycle.startDate.plusDays(avgLength.roundToLong())
        // Janela de incerteza: ±1 desvio padrão, com no mínimo 2 e no máximo 7 dias para
        // nunca passar uma falsa sensação de precisão quando há pouco histórico.
        val uncertaintyDays = stdDev.roundToLong().coerceIn(2, 7)

        val lutealPhaseLength = 14
        val ovulationDay = nextPeriodStart.minusDays(lutealPhaseLength.toLong())
        val fertileStart = ovulationDay.minusDays(5)
        val fertileEnd = ovulationDay.plusDays(1)

        val confidence = when {
            completedCycles == 0 -> PredictionConfidence.INSUFICIENTE
            completedCycles in 1..2 -> PredictionConfidence.BAIXA
            completedCycles in 3..5 -> PredictionConfidence.MEDIA
            else -> PredictionConfidence.ALTA
        }

        return CyclePrediction(
            nextPeriodStartEstimate = nextPeriodStart,
            nextPeriodStartRangeEarliest = nextPeriodStart.minusDays(uncertaintyDays),
            nextPeriodStartRangeLatest = nextPeriodStart.plusDays(uncertaintyDays),
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            estimatedOvulationDay = ovulationDay,
            averageCycleLength = avgLength,
            cycleLengthStdDeviation = stdDev,
            confidenceLevel = confidence,
            cyclesUsedForPrediction = completedCycles
        )
    }
}

/**
 * Cruza o histórico de humor/energia/sintomas com a fase do ciclo em que cada
 * registro foi feito, revelando padrões próprios da usuária (ex.: "energia mais
 * baixa na fase lútea", "cólicas mais fortes nos 2 primeiros dias").
 */
class GenerateHolisticInsightsUseCase @Inject constructor(
    private val periodRepository: PeriodRepository,
    private val wellbeingRepository: WellbeingRepository,
    private val symptomRepository: SymptomRepository
) {
    suspend operator fun invoke(): List<PhaseWellbeingInsight> {
        val entries = periodRepository.getAllEntriesOnce()
        val cycles = CycleCalculator.buildCycles(entries)
        if (cycles.size < 2) return emptyList() // precisa de histórico mínimo pra gerar insight confiável

        val avgLength = CycleCalculator.weightedAverageCycleLength(cycles)
        val avgPeriodLength = CycleCalculator.averagePeriodLength(cycles)

        val wellbeingLogs = wellbeingRepository.getAllLogsOnce()
        val symptomLogs = symptomRepository.getAllSymptomsOnce()

        fun phaseOf(date: LocalDate) =
            CycleCalculator.classifyPhase(date, cycles, avgLength, avgPeriodLength)

        return CyclePhase.entries.filter { it != CyclePhase.INDEFINIDA }.mapNotNull { phase ->
            val logsInPhase = wellbeingLogs.filter { phaseOf(it.date) == phase }
            if (logsInPhase.isEmpty()) return@mapNotNull null

            val avgEnergy = logsInPhase.map { it.energyLevel }.average()
            val topMoods = logsInPhase.flatMap { it.moods }
                .groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }
                .take(3).map { it.key }

            val symptomsInPhase = symptomLogs.filter { phaseOf(it.date) == phase }
            val topSymptoms = symptomsInPhase.groupingBy { it.type }.eachCount()
                .entries.sortedByDescending { it.value }
                .take(3).map { it.key }

            PhaseWellbeingInsight(
                phase = phase,
                averageEnergyLevel = avgEnergy,
                mostFrequentMoods = topMoods,
                mostFrequentSymptoms = topSymptoms,
                sampleSize = logsInPhase.size
            )
        }
    }
}
