package com.jonathaxs.gymnutshell.health

import androidx.annotation.StringRes
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.jonathaxs.gymnutshell.R

/** Categoria de treino pro auto check-in: meta de Treino (força) ou meta de Cardio. */
enum class WorkoutCategory { Strength, Cardio }

/**
 * Classifica os tipos de exercício do Health Connect em força ou cardio — porte dos conjuntos
 * `strengthActivityTypes`/`cardioActivityTypes` do HealthKitManager (iOS). Tipos sem equivalente
 * direto no Health Connect (ex.: pular corda) ou não mapeados caem em `null` e são ignorados.
 */
object ExerciseClassifier {

    // Tipos que contam como Treino de musculação (força).
    private val strengthTypes: Set<Int> = setOf(
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING,
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
        ExerciseSessionRecord.EXERCISE_TYPE_BOOT_CAMP,
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING,
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING,
        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS,
        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS,
        ExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING,
    )

    // Tipos que contam como Cardio.
    private val cardioTypes: Set<Int> = setOf(
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING,
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL,
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER,
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL,
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING,
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE,
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING,
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING_MACHINE,
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING,
        ExerciseSessionRecord.EXERCISE_TYPE_DANCING,
        ExerciseSessionRecord.EXERCISE_TYPE_SKIING,
        ExerciseSessionRecord.EXERCISE_TYPE_SNOWBOARDING,
        ExerciseSessionRecord.EXERCISE_TYPE_SKATING,
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_SKATING,
        ExerciseSessionRecord.EXERCISE_TYPE_PADDLING,
        ExerciseSessionRecord.EXERCISE_TYPE_SURFING,
        ExerciseSessionRecord.EXERCISE_TYPE_SAILING,
        ExerciseSessionRecord.EXERCISE_TYPE_WATER_POLO,
        ExerciseSessionRecord.EXERCISE_TYPE_SCUBA_DIVING,
        ExerciseSessionRecord.EXERCISE_TYPE_SOCCER,
        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_TENNIS,
        ExerciseSessionRecord.EXERCISE_TYPE_TABLE_TENNIS,
        ExerciseSessionRecord.EXERCISE_TYPE_VOLLEYBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_SOFTBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AMERICAN,
        ExerciseSessionRecord.EXERCISE_TYPE_FOOTBALL_AUSTRALIAN,
        ExerciseSessionRecord.EXERCISE_TYPE_RUGBY,
        ExerciseSessionRecord.EXERCISE_TYPE_ICE_HOCKEY,
        ExerciseSessionRecord.EXERCISE_TYPE_ROLLER_HOCKEY,
        ExerciseSessionRecord.EXERCISE_TYPE_RACQUETBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_SQUASH,
        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON,
        ExerciseSessionRecord.EXERCISE_TYPE_HANDBALL,
        ExerciseSessionRecord.EXERCISE_TYPE_GOLF,
    )

    /** Retorna a categoria do exercício, ou null quando ele não conta como treino nem cardio. */
    fun categoryOf(exerciseType: Int): WorkoutCategory? = when (exerciseType) {
        in strengthTypes -> WorkoutCategory.Strength
        in cardioTypes -> WorkoutCategory.Cardio
        else -> null
    }

    /** Nome de exibição da atividade (porte do `displayName(for:)` do iOS). */
    @StringRes
    fun displayNameRes(exerciseType: Int): Int = when (exerciseType) {
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> R.string.health_activity_running
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> R.string.health_activity_walking
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> R.string.health_activity_cycling
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> R.string.health_activity_swimming
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> R.string.health_activity_elliptical
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING,
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> R.string.health_activity_rowing
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING,
        ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING_MACHINE -> R.string.health_activity_stair_climbing
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> R.string.health_activity_hiking
        ExerciseSessionRecord.EXERCISE_TYPE_DANCING -> R.string.health_activity_dance
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
        ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> R.string.health_activity_strength_training
        ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> R.string.health_activity_functional_training
        ExerciseSessionRecord.EXERCISE_TYPE_BOOT_CAMP -> R.string.health_activity_cross_training
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> R.string.health_activity_hiit
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING -> R.string.health_activity_boxing
        ExerciseSessionRecord.EXERCISE_TYPE_MARTIAL_ARTS -> R.string.health_activity_martial_arts
        ExerciseSessionRecord.EXERCISE_TYPE_ROCK_CLIMBING -> R.string.health_activity_climbing
        else -> R.string.health_activity_other
    }
}
