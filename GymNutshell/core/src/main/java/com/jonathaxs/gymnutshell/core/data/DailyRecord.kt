package com.jonathaxs.gymnutshell.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Um dia concluído no Gym Nutshell — porte do @Model DailyRecord (SwiftData) pro Room @Entity.
 *
 * Diferenças do iOS:
 * - `date` é o **epoch-day** (dias desde 1970-01-01, via LocalDate.toEpochDay) e serve de chave
 *   primária: garante 1 registro/dia e é imune a fuso horário (representa o dia do calendário, não um instante).
 * - `customValues`/`customRestDays` eram `Data` (JSON) no iOS; aqui são String JSON
 *   (serialização fica na camada de dados/domínio, com kotlinx.serialization).
 * - Os nomes antigos (`carb`, `fat`, `catTitle`, `catEmoji`) eram migrações do SwiftData;
 *   num banco novo usamos os nomes finais direto.
 */
@Entity(tableName = "daily_record")
data class DailyRecord(
    @PrimaryKey val date: Long, // epoch-day (LocalDate.toEpochDay)
    val water: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val goodFat: Int = 0,
    val fiber: Int = 0,
    val sleep: Int = 0,
    /** [String: Int] em JSON — ingestão de cada meta personalizada no dia. */
    val customValues: String = "{}",
    val didWorkout: Boolean = false,
    val didCardio: Boolean = false,
    val workoutRestDay: Boolean = false,
    val cardioRestDay: Boolean = false,
    /** [String: Boolean] em JSON — estado de "dia de descanso" das metas personalizadas. */
    val customRestDays: String = "{}",
    val percent: Int = 0,
    val achievementTitle: String = "",
    val achievementEmoji: String = "",
    val points: Int = 0,
)
