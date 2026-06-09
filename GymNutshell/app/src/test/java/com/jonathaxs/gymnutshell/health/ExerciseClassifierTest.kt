package com.jonathaxs.gymnutshell.health

import androidx.health.connect.client.records.ExerciseSessionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Garante que os tipos de exercício do Health Connect caem na categoria certa (força/cardio/ignorado). */
class ExerciseClassifierTest {

    @Test
    fun musculacao_eh_forca() {
        assertEquals(WorkoutCategory.Strength, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING))
        assertEquals(WorkoutCategory.Strength, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING))
    }

    @Test
    fun hiit_e_boxe_sao_forca() {
        assertEquals(WorkoutCategory.Strength, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING))
        assertEquals(WorkoutCategory.Strength, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_BOXING))
    }

    @Test
    fun corrida_e_bike_sao_cardio() {
        assertEquals(WorkoutCategory.Cardio, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_RUNNING))
        assertEquals(WorkoutCategory.Cardio, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_BIKING))
    }

    @Test
    fun esportes_de_quadra_sao_cardio() {
        assertEquals(WorkoutCategory.Cardio, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_SOCCER))
        assertEquals(WorkoutCategory.Cardio, ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_TENNIS))
    }

    @Test
    fun tipo_nao_mapeado_eh_ignorado() {
        // Yoga e meditação não contam como treino nem cardio no app.
        assertNull(ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_YOGA))
        assertNull(ExerciseClassifier.categoryOf(ExerciseSessionRecord.EXERCISE_TYPE_PILATES))
    }
}
