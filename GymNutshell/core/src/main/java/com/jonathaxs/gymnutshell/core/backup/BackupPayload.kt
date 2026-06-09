package com.jonathaxs.gymnutshell.core.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estrutura raiz do arquivo JSON de backup — porte do BackupPayload (iOS), mantida
 * **compatível com o formato v4 do iOS** pra permitir migração entre aparelhos.
 *
 * Campos obrigatórios no iOS (version, exportedAt, profile, goals, goalsOrder, customGoals,
 * dailyRecords) ficam sem default → são sempre emitidos. Os opcionais ficam nuláveis com
 * default null → somem do JSON quando vazios (espelha o `nil` do iOS, via `encodeDefaults = false`).
 *
 * Campos iOS-only que o Android ainda não tem (removedItems, customCategories, categoryOrder,
 * builtinCategoryOrder) não entram aqui: no export ficam ausentes (iOS lê como nil) e no import
 * são ignorados (via `ignoreUnknownKeys`).
 */
@Serializable
data class BackupPayload(
    val version: Int,
    val exportedAt: String, // ISO8601
    val profile: ProfileSnapshot,
    val goals: GoalsSnapshot,
    val goalsOrder: List<String>,
    val customGoals: List<CustomGoalSnapshot>,
    val dailyRecords: List<RecordSnapshot>,
    val appearance: AppearanceSnapshot? = null,
    val preferences: PreferencesSnapshot? = null,
)

/** Dados de perfil. A chave JSON `fitnessGoal` é legada (era o nome antes do rename userGoal). */
@Serializable
data class ProfileSnapshot(
    val name: String,
    val username: String? = null,
    val height: Int,
    val weight: Double,
    val age: Int,
    val sex: String,
    @SerialName("fitnessGoal") val userGoal: String,
    val measurementSystem: String? = null,
)

/** Alvos das metas fixas. `fats` é a chave legada de goodFat. */
@Serializable
data class GoalsSnapshot(
    val calories: Int,
    val sleep: Int,
    val water: Int,
    val protein: Int,
    val carbs: Int,
    @SerialName("fats") val goodFat: Int,
    val fiber: Int,
)

/** Meta personalizada. `id` é String pra casar com o UUID do iOS (no Android é o Long convertido). */
@Serializable
data class CustomGoalSnapshot(
    val id: String,
    val emoji: String,
    val name: String,
    val unit: String,
    val goal: Int,
    val increment: Int,
    val category: String? = null,
    val customCategoryId: String? = null,
)

/** Um DailyRecord serializado. Chaves legadas: `carb`, `fat`, `catTitle`, `catEmoji`. */
@Serializable
data class RecordSnapshot(
    val date: String, // ISO8601
    val water: Int,
    val protein: Int,
    @SerialName("carb") val carbs: Int,
    @SerialName("fat") val goodFat: Int,
    val fiber: Int,
    val sleep: Int,
    val percent: Int,
    @SerialName("catTitle") val achievementTitle: String,
    @SerialName("catEmoji") val achievementEmoji: String,
    val points: Int,
    val didWorkout: Boolean,
    val didCardio: Boolean? = null,
    val customValues: Map<String, Int>,
)

/** Tema e cor de destaque. */
@Serializable
data class AppearanceSnapshot(
    val theme: String,
    val accentColor: String,
)

/**
 * Preferências diversas (tudo opcional, espelha o iOS). Widget e orientationLock não existem
 * no Android ainda → ficam null. As chaves dos mapas de notificação são o id do tipo
 * ("<kind>" ou "custom.<id>"), iguais ao esquema do DataStore do Android.
 */
@Serializable
data class PreferencesSnapshot(
    val widgetBackground: String? = null,
    val widgetBackgroundMode: String? = null,
    val orientationLock: String? = null,
    val autoWorkoutCheckin: Boolean? = null,
    val notificationEnabled: Map<String, Boolean>? = null,
    val notificationInterval: Map<String, Int>? = null,
    val notificationSound: Map<String, String>? = null,
    val goalIncrements: Map<String, Int>? = null,
)
