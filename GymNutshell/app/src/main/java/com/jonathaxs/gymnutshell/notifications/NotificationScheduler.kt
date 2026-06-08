package com.jonathaxs.gymnutshell.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.IntakeRepository
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Agenda os lembretes por intervalo via WorkManager — porte da lógica de reschedule/cancel do
 * NotificationManager (iOS).
 *
 * Diferença de estratégia: o iOS pré-agenda 4 one-shots (UNCalendarNotificationTrigger); aqui usamos
 * um worker que **se reencadeia** ([ReminderWorker]): cada disparo posta o lembrete e agenda o
 * próximo dentro da janela de horário do kind. A cadeia para sozinha quando o kind é desligado,
 * a meta do dia é batida, ou vira dia de descanso.
 */
class NotificationScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)
    private val prefs = NotificationPreferencesRepository(appContext)
    private val intakeRepo = IntakeRepository(appContext)
    private val profileRepo = ProfileRepository(appContext)
    private val customRepo = CustomGoalRepository(appContext)

    // MARK: - Kind fixo

    /** Reavalia um kind: agenda o próximo lembrete se ainda deve lembrar, senão cancela. */
    suspend fun reschedule(kind: NotificationKind) {
        if (!kind.isIntervalBased) return
        if (shouldRemind(kind)) scheduleNext(kind) else cancel(kind)
    }

    /** Cancela os lembretes pendentes de um kind. */
    fun cancel(kind: NotificationKind) {
        workManager.cancelUniqueWork(workName(kind))
    }

    /** True se o lembrete deve disparar: habilitado, não é dia de descanso e a meta não foi batida. */
    suspend fun shouldRemind(kind: NotificationKind): Boolean {
        if (!kind.isIntervalBased || !prefs.isEnabled(kind)) return false
        val tracking = kind.trackingKey ?: return true // Progresso: sem meta única, sempre lembra
        if (tracking in intakeRepo.restDays.first()) return false
        val target = targetFor(tracking) ?: return true
        if (target <= 0) return true
        val intake = intakeRepo.intakes.first()[tracking] ?: 0
        return intake < target
    }

    /** Enfileira o próximo disparo do kind (sem checagens — quem chama já validou). */
    suspend fun scheduleNext(kind: NotificationKind) {
        val delay = nextDelayMillis(prefs.intervalMinutes(kind), kind.dailyStartHour, kind.dailyCutoffHour)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to kind.rawValue))
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(workName(kind), ExistingWorkPolicy.REPLACE, request)
    }

    // MARK: - Metas personalizadas

    suspend fun rescheduleCustom(id: Long) {
        if (shouldRemindCustom(id)) scheduleNextCustom(id) else cancelCustom(id)
    }

    fun cancelCustom(id: Long) {
        workManager.cancelUniqueWork(workNameCustom(id))
    }

    suspend fun shouldRemindCustom(id: Long): Boolean {
        if (!prefs.isCustomEnabled(id)) return false
        val goal = customGoal(id) ?: return false
        if (goal.intakeKey in intakeRepo.restDays.first()) return false
        if (goal.target <= 0) return true
        val intake = intakeRepo.intakes.first()[goal.intakeKey] ?: 0
        return intake < goal.target
    }

    suspend fun scheduleNextCustom(id: Long) {
        // Metas personalizadas usam a janela padrão 6h–22h (igual ao iOS).
        val delay = nextDelayMillis(prefs.customIntervalMinutes(id), startHour = 6, cutoffHour = 22)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_CUSTOM_ID to id))
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(workNameCustom(id), ExistingWorkPolicy.REPLACE, request)
    }

    suspend fun customGoal(id: Long): CustomGoal? = customRepo.all().firstOrNull { it.id == id }

    // MARK: - Geral

    /** Re-arma todos os lembretes ativos — porte de rescheduleAllActive (iOS). Chamado no launch. */
    suspend fun rescheduleAllActive() {
        NotificationKind.entries.filter { it.isIntervalBased }.forEach { reschedule(it) }
        customRepo.all().forEach { rescheduleCustom(it.id) }
    }

    // MARK: - Privado

    /**
     * Milissegundos até o próximo disparo: agora + intervalo, ajustado pra cair dentro da janela
     * [startHour, cutoffHour). Antes do início → começa no startHour de hoje; depois do corte →
     * startHour de amanhã.
     */
    private fun nextDelayMillis(intervalMinutes: Int, startHour: Int, cutoffHour: Int): Long {
        val now = LocalDateTime.now()
        val candidate = now.plusMinutes(intervalMinutes.toLong())
        val adjusted = when {
            candidate.hour < startHour -> candidate.toLocalDate().atTime(startHour, 0)
            candidate.hour >= cutoffHour -> candidate.toLocalDate().plusDays(1).atTime(startHour, 0)
            else -> candidate
        }
        return Duration.between(now, adjusted).toMillis().coerceAtLeast(1_000L)
    }

    /** Alvo (em unidade armazenada) da meta fixa, ou null se não houver. */
    private suspend fun targetFor(trackingKey: String): Int? {
        val profile = profileRepo.profile.first()
        val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
        val result = GoalsProvider.goals(effective)
        return BuiltInGoals.forResult(result).firstOrNull { it.key == trackingKey }?.target
    }

    private fun workName(kind: NotificationKind) = "reminder.${kind.rawValue}"
    private fun workNameCustom(id: Long) = "reminder.custom.$id"

    private companion object {
        const val TAG = "gn.reminder"

        // Mesmo perfil-demo da TodayViewModel, até o onboarding preencher os dados reais.
        val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
