package com.jonathaxs.gymnutshell.notifications

import androidx.annotation.StringRes
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.domain.NotificationSound

/**
 * Mapeia cada NotificationKind/Sound pros recursos de string (strings.xml).
 * No iOS o Core resolvia via chaves dotted; no Android os nomes de recurso não podem ter ponto,
 * então centralizamos o `when()` aqui — reutilizado pelo GymNotifier e pela tela de Ajustes.
 */
object NotificationStrings {

    @StringRes
    fun titleRes(kind: NotificationKind): Int = when (kind) {
        NotificationKind.Progress -> R.string.notif_kind_progress_title
        NotificationKind.Achievement -> R.string.notif_kind_achievement_title
        NotificationKind.StreakBonus -> R.string.notif_kind_streak_title
        NotificationKind.HealthSync -> R.string.notif_kind_health_title
        NotificationKind.Backup -> R.string.notif_kind_backup_title
        NotificationKind.Sleep -> R.string.notif_kind_sleep_title
        NotificationKind.Water -> R.string.notif_kind_water_title
        NotificationKind.Calories -> R.string.notif_kind_calories_title
        NotificationKind.Protein -> R.string.notif_kind_protein_title
        NotificationKind.Carbs -> R.string.notif_kind_carbs_title
        NotificationKind.GoodFat -> R.string.notif_kind_goodfat_title
        NotificationKind.Fiber -> R.string.notif_kind_fiber_title
        NotificationKind.Workout -> R.string.notif_kind_workout_title
        NotificationKind.Cardio -> R.string.notif_kind_cardio_title
        NotificationKind.Creatine -> R.string.notif_kind_creatine_title
    }

    @StringRes
    fun descRes(kind: NotificationKind): Int = when (kind) {
        NotificationKind.Progress -> R.string.notif_kind_progress_desc
        NotificationKind.Achievement -> R.string.notif_kind_achievement_desc
        NotificationKind.StreakBonus -> R.string.notif_kind_streak_desc
        NotificationKind.HealthSync -> R.string.notif_kind_health_desc
        NotificationKind.Backup -> R.string.notif_kind_backup_desc
        NotificationKind.Sleep -> R.string.notif_kind_sleep_desc
        NotificationKind.Water -> R.string.notif_kind_water_desc
        NotificationKind.Calories -> R.string.notif_kind_calories_desc
        NotificationKind.Protein -> R.string.notif_kind_protein_desc
        NotificationKind.Carbs -> R.string.notif_kind_carbs_desc
        NotificationKind.GoodFat -> R.string.notif_kind_goodfat_desc
        NotificationKind.Fiber -> R.string.notif_kind_fiber_desc
        NotificationKind.Workout -> R.string.notif_kind_workout_desc
        NotificationKind.Cardio -> R.string.notif_kind_cardio_desc
        NotificationKind.Creatine -> R.string.notif_kind_creatine_desc
    }

    /** Corpo do lembrete recorrente. Só faz sentido pros kinds baseados em intervalo. */
    @StringRes
    fun bodyRes(kind: NotificationKind): Int = when (kind) {
        NotificationKind.Progress -> R.string.notif_kind_progress_body
        NotificationKind.Sleep -> R.string.notif_kind_sleep_body
        NotificationKind.Water -> R.string.notif_kind_water_body
        NotificationKind.Calories -> R.string.notif_kind_calories_body
        NotificationKind.Protein -> R.string.notif_kind_protein_body
        NotificationKind.Carbs -> R.string.notif_kind_carbs_body
        NotificationKind.GoodFat -> R.string.notif_kind_goodfat_body
        NotificationKind.Fiber -> R.string.notif_kind_fiber_body
        NotificationKind.Workout -> R.string.notif_kind_workout_body
        NotificationKind.Cardio -> R.string.notif_kind_cardio_body
        NotificationKind.Creatine -> R.string.notif_kind_creatine_body
        // Kinds de evento não têm corpo recorrente; caem no progresso por segurança.
        else -> R.string.notif_kind_progress_body
    }

    /** Rodapé explicativo do editor de intervalo (Ajustes). */
    @StringRes
    fun footerRes(kind: NotificationKind): Int = when (kind) {
        NotificationKind.Progress -> R.string.notif_kind_progress_footer
        NotificationKind.Sleep -> R.string.notif_kind_sleep_footer
        NotificationKind.Water -> R.string.notif_kind_water_footer
        NotificationKind.Calories -> R.string.notif_kind_calories_footer
        NotificationKind.Protein -> R.string.notif_kind_protein_footer
        NotificationKind.Carbs -> R.string.notif_kind_carbs_footer
        NotificationKind.GoodFat -> R.string.notif_kind_goodfat_footer
        NotificationKind.Fiber -> R.string.notif_kind_fiber_footer
        NotificationKind.Workout -> R.string.notif_kind_workout_footer
        NotificationKind.Cardio -> R.string.notif_kind_cardio_footer
        NotificationKind.Creatine -> R.string.notif_kind_creatine_footer
        else -> R.string.notif_kind_progress_footer
    }

    @StringRes
    fun soundRes(sound: NotificationSound): Int = when (sound) {
        NotificationSound.Default -> R.string.notif_sound_default
        NotificationSound.Silent -> R.string.notif_sound_silent
    }
}
