package com.ciclovida.app.domain.model

import java.time.LocalDate

/** Um dia de menstruação registrado (vários dias seguidos formam um "período"). */
data class PeriodDayEntry(
    val id: Long = 0,
    val date: LocalDate,
    val flowIntensity: FlowIntensity
)

enum class FlowIntensity { SPOTTING, LEVE, MODERADO, INTENSO }

/** Um ciclo completo, derivado dos PeriodDayEntry (do início de um período até o início do próximo). */
data class Cycle(
    val startDate: LocalDate,
    val endDate: LocalDate?, // null = ciclo em andamento (o mais recente)
    val lengthInDays: Int?,
    val periodLengthInDays: Int
)

enum class SymptomType {
    COLICA, DOR_DE_CABECA, INCHACO, SENSIBILIDADE_MAMAS, ACNE, FADIGA,
    NAUSEA, DOR_NAS_COSTAS, ALTERACAO_APETITE, INSONIA
}

data class SymptomLog(
    val id: Long = 0,
    val date: LocalDate,
    val type: SymptomType,
    val intensity: Int // 1 (leve) a 3 (forte)
)

enum class Mood { FELIZ, CALMA, IRRITADA, TRISTE, ANSIOSA, SENSIVEL, ENERGICA, CANSADA }

data class DailyWellbeingLog(
    val id: Long = 0,
    val date: LocalDate,
    val moods: List<Mood>,
    val energyLevel: Int, // 1 (muito baixa) a 5 (muito alta)
    val notes: String? = null
)

/** Fase do ciclo em uma data específica, usada para cruzar com humor/energia nos Insights. */
enum class CyclePhase { MENSTRUAL, FOLICULAR, OVULATORIA, LUTEA, INDEFINIDA }

/**
 * Previsão calculada a partir do histórico de ciclos da própria usuária.
 * Quanto mais ciclos registrados, maior a confiança (confidenceLevel).
 */
data class CyclePrediction(
    val nextPeriodStartEstimate: LocalDate,
    val nextPeriodStartRangeEarliest: LocalDate,
    val nextPeriodStartRangeLatest: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate,
    val estimatedOvulationDay: LocalDate,
    val averageCycleLength: Double,
    val cycleLengthStdDeviation: Double,
    val confidenceLevel: PredictionConfidence,
    val cyclesUsedForPrediction: Int
)

enum class PredictionConfidence { INSUFICIENTE, BAIXA, MEDIA, ALTA }

/** Resultado da análise holística: correlação entre fase do ciclo e bem-estar. */
data class PhaseWellbeingInsight(
    val phase: CyclePhase,
    val averageEnergyLevel: Double,
    val mostFrequentMoods: List<Mood>,
    val mostFrequentSymptoms: List<SymptomType>,
    val sampleSize: Int
)
