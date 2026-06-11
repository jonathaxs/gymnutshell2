package com.jonathaxs.gymnutshell.core.sync

import android.content.Context
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Constrói (no celular) e aplica (no relógio) o WearSnapshot usando os repos do :core.
 * Fica no :core pra ser testável e livre de Play Services; quem fala com o Data Layer
 * são as camadas :app (remetente) e :wear (receptor).
 */
object WearSnapshotSync {

    /** Lê o estado atual dos repos e monta o snapshot (lado celular). */
    suspend fun build(context: Context, nowMillis: Long = System.currentTimeMillis()): WearSnapshot {
        val profile = ProfileRepository(context).profile.first()
        val settings = SettingsRepository(context)
        val intakeRepo = IntakeRepository(context)

        return WearSnapshot(
            sentAtMillis = nowMillis,
            epochDay = LocalDate.now().toEpochDay(),
            intakes = intakeRepo.intakes.first(),
            restDays = intakeRepo.restDays.first().toList(),
            profile = WearProfileSnapshot(
                name = profile.name,
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                age = profile.age,
                sex = profile.sex,
                userGoalRaw = profile.goal.rawValue,
            ),
            themeRaw = settings.theme.first().rawValue,
            accentName = settings.accentColor.first().name,
            customGoals = CustomGoalRepository(context).all().map { c ->
                WearCustomGoalSnapshot(
                    id = c.id,
                    emoji = c.emoji,
                    name = c.name,
                    unit = c.unit,
                    target = c.target,
                    increment = c.increment,
                    categoryRaw = c.categoryRaw,
                )
            },
        )
    }

    /**
     * Aplica o snapshot recebido nos repos locais (lado relógio) — porte do
     * applySnapshot do iOS. O snapshot é a fonte da verdade: intakes/descansos
     * são substituídos por inteiro e as metas custom são espelhadas com o mesmo id.
     */
    suspend fun apply(context: Context, snapshot: WearSnapshot) {
        val intakeRepo = IntakeRepository(context)
        val settings = SettingsRepository(context)

        intakeRepo.replaceAll(snapshot.intakes, snapshot.restDays.toSet())
        // Alinha a virada de dia do relógio com o dia do celular, assim o rollover
        // local não zera um snapshot recém-chegado.
        intakeRepo.setLastActiveDay(snapshot.epochDay)

        ProfileRepository(context).update(
            Profile(
                name = snapshot.profile.name,
                weightKg = snapshot.profile.weightKg,
                heightCm = snapshot.profile.heightCm,
                age = snapshot.profile.age,
                sex = snapshot.profile.sex,
                goal = UserGoal.fromRaw(snapshot.profile.userGoalRaw) ?: UserGoal.Maintenance,
            ),
        )
        settings.setTheme(AppTheme.fromRaw(snapshot.themeRaw))
        runCatching { AccentColor.valueOf(snapshot.accentName) }.getOrNull()?.let {
            settings.setAccentColor(it)
        }
        CustomGoalRepository(context).replaceAll(
            snapshot.customGoals.map { c ->
                CustomGoal(
                    id = c.id,
                    emoji = c.emoji,
                    name = c.name,
                    unit = c.unit,
                    target = c.target,
                    increment = c.increment,
                    categoryRaw = c.categoryRaw,
                )
            },
        )
    }
}
