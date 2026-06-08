package com.jonathaxs.gymnutshell.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jonathaxs.gymnutshell.MainActivity
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.core.domain.NotificationKind
import com.jonathaxs.gymnutshell.core.domain.NotificationRoute
import com.jonathaxs.gymnutshell.core.domain.NotificationSound
import java.util.concurrent.atomic.AtomicInteger

/**
 * Centraliza o disparo de notificações locais no Android — porte do NotificationManager (iOS).
 *
 * Esta fatia (5A-2) cobre os **eventos** (conquista, bônus de sequência, saúde, backup) via
 * NotificationManagerCompat. Os lembretes por intervalo (agendados) entram na 5A-3 (WorkManager).
 *
 * Diferenças de plataforma:
 * - **Canais** (API 26+, obrigatórios): o som é propriedade do canal, não da notificação. Pra honrar
 *   o toggle "Default/Silent" por kind, mantemos 2 canais por kind (com som / silencioso) e
 *   escolhemos o canal na hora de postar. Canais ficam em 2 grupos (Sistema / Lembretes), espelhando
 *   as seções de Ajustes do iOS.
 * - **Permissão** POST_NOTIFICATIONS (Android 13+): se não concedida, `areNotificationsEnabled()`
 *   é false e o post vira no-op (sem crash).
 */
class GymNotifier(context: Context) {

    private val appContext = context.applicationContext
    private val manager = NotificationManagerCompat.from(appContext)
    private val prefs = NotificationPreferencesRepository(appContext)

    // MARK: - Eventos

    /** Conquista do dia ("Você terminou o dia como …"). Porte de fireAchievementUnlocked (iOS). */
    suspend fun fireAchievementUnlocked(tierName: String, emoji: String, achievementEpochDay: Long?) {
        if (!prefs.isEnabled(NotificationKind.Achievement)) return
        post(
            kind = NotificationKind.Achievement,
            title = appContext.getString(R.string.notif_kind_achievement_title),
            body = appContext.getString(R.string.notif_event_achievement_body, emoji, tierName),
            achievementEpochDay = achievementEpochDay,
            sound = prefs.sound(NotificationKind.Achievement),
        )
    }

    /** Bônus de sequência (semanal/mensal). Porte de fireStreakBonus (iOS). */
    suspend fun fireStreakBonus(emoji: String, points: Int) {
        if (!prefs.isEnabled(NotificationKind.StreakBonus)) return
        post(
            kind = NotificationKind.StreakBonus,
            title = appContext.getString(R.string.notif_kind_streak_title),
            body = appContext.getString(R.string.notif_event_streak_body, emoji, points),
            sound = prefs.sound(NotificationKind.StreakBonus),
        )
    }

    enum class HealthLogKind { Cardio, Workout, Sleep }

    /** Confirmação de dado sincronizado do Health Connect. Porte de fireHealthLogged (iOS). */
    suspend fun fireHealthLogged(kind: HealthLogKind, value: Int, activityName: String? = null) {
        if (!prefs.isEnabled(NotificationKind.HealthSync)) return
        val activity = activityName ?: appContext.getString(R.string.health_activity_other)
        val body = when (kind) {
            HealthLogKind.Cardio -> appContext.getString(R.string.notif_event_health_body_cardio, value, activity)
            HealthLogKind.Workout -> appContext.getString(R.string.notif_event_health_body_workout, value, activity)
            HealthLogKind.Sleep -> appContext.getString(R.string.notif_event_health_body_sleep, value)
        }
        post(
            kind = NotificationKind.HealthSync,
            title = appContext.getString(R.string.notif_kind_health_title),
            body = body,
            sound = prefs.sound(NotificationKind.HealthSync),
        )
    }

    /** Backup concluído. Porte de fireBackupCompleted (iOS). */
    suspend fun fireBackupCompleted() {
        if (!prefs.isEnabled(NotificationKind.Backup)) return
        post(
            kind = NotificationKind.Backup,
            title = appContext.getString(R.string.notif_kind_backup_title),
            body = appContext.getString(R.string.notif_event_backup_body),
            sound = prefs.sound(NotificationKind.Backup),
        )
    }

    // MARK: - Lembretes por intervalo (agendados via WorkManager, fatia 5A-3)

    /** Lembrete recorrente de um kind fixo (água, proteína, …). Postado pelo ReminderWorker. */
    suspend fun postReminder(kind: NotificationKind) {
        val sound = prefs.sound(kind)
        postRaw(
            channelId = ensureChannel(kind, sound),
            title = appContext.getString(NotificationStrings.titleRes(kind)),
            body = appContext.getString(NotificationStrings.bodyRes(kind)),
            route = kind.route,
            sound = sound,
        )
    }

    /** Lembrete recorrente de uma meta personalizada. Postado pelo ReminderWorker. */
    suspend fun postCustomReminder(goal: CustomGoal) {
        val sound = prefs.customSound(goal.id)
        postRaw(
            channelId = ensureCustomChannel(goal, sound),
            title = "${goal.emoji} ${goal.name}",
            body = appContext.getString(R.string.notif_custom_body, goal.name),
            route = NotificationRoute.Today,
            sound = sound,
        )
    }

    // MARK: - Núcleo de postagem

    /** Posta uma notificação imediata no canal do kind/som, com deep-link via PendingIntent. */
    private fun post(
        kind: NotificationKind,
        title: String,
        body: String,
        achievementEpochDay: Long? = null,
        sound: NotificationSound,
    ) {
        postRaw(ensureChannel(kind, sound), title, body, kind.route, achievementEpochDay, sound)
    }

    /** Postagem genérica: canal + deep-link via PendingIntent. */
    private fun postRaw(
        channelId: String,
        title: String,
        body: String,
        route: NotificationRoute,
        achievementEpochDay: Long? = null,
        sound: NotificationSound,
    ) {
        if (!manager.areNotificationsEnabled()) return
        val notifId = idCounter.incrementAndGet()

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_ROUTE, route.rawValue)
            achievementEpochDay?.let { putExtra(EXTRA_ACHIEVEMENT_DAY, it) }
        }
        // requestCode único (= notifId) evita que PendingIntents colidam e percam os extras.
        val pendingIntent = PendingIntent.getActivity(
            appContext, notifId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (sound == NotificationSound.Silent) NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_DEFAULT,
            )
            .build()

        manager.notify(notifId, notification)
    }

    // MARK: - Canais

    /**
     * Garante grupo + canal (idempotente) e devolve o channelId. Há 2 canais por kind: com som
     * (IMPORTANCE_DEFAULT) e silencioso (IMPORTANCE_LOW), escolhidos pelo [sound].
     */
    private fun ensureChannel(kind: NotificationKind, sound: NotificationSound): String {
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_REMINDERS, appContext.getString(R.string.notif_group_reminders)),
        )
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_SYSTEM, appContext.getString(R.string.notif_group_system)),
        )

        val silent = sound == NotificationSound.Silent
        val channelId = "kind.${kind.rawValue}" + if (silent) ".silent" else ""
        val baseName = appContext.getString(NotificationStrings.titleRes(kind))
        val name = if (silent) "$baseName (${appContext.getString(R.string.notif_sound_silent)})" else baseName
        val importance = if (silent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, name, importance).apply {
            description = appContext.getString(NotificationStrings.descRes(kind))
            group = if (kind in EVENT_KINDS) GROUP_SYSTEM else GROUP_REMINDERS
            if (silent) setSound(null, null)
        }
        manager.createNotificationChannel(channel)
        return channelId
    }

    /** Garante o canal de uma meta personalizada (grupo Lembretes) e devolve o channelId. */
    private fun ensureCustomChannel(goal: CustomGoal, sound: NotificationSound): String {
        manager.createNotificationChannelGroup(
            NotificationChannelGroup(GROUP_REMINDERS, appContext.getString(R.string.notif_group_reminders)),
        )
        val silent = sound == NotificationSound.Silent
        val channelId = "custom.${goal.id}" + if (silent) ".silent" else ""
        val baseName = "${goal.emoji} ${goal.name}"
        val name = if (silent) "$baseName (${appContext.getString(R.string.notif_sound_silent)})" else baseName
        val importance = if (silent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, name, importance).apply {
            description = appContext.getString(R.string.notif_custom_desc)
            group = GROUP_REMINDERS
            if (silent) setSound(null, null)
        }
        manager.createNotificationChannel(channel)
        return channelId
    }

    companion object {
        /** Extras do Intent de deep-link, lidos na MainActivity (roteamento na 5A-5). */
        const val EXTRA_ROUTE = "gn.notif.route"
        const val EXTRA_ACHIEVEMENT_DAY = "gn.notif.achievementDay"

        private const val GROUP_REMINDERS = "reminders"
        private const val GROUP_SYSTEM = "system"

        // Kinds de evento ficam no grupo "Sistema"; o resto (metas + progresso) em "Lembretes".
        private val EVENT_KINDS = setOf(
            NotificationKind.Achievement, NotificationKind.StreakBonus,
            NotificationKind.HealthSync, NotificationKind.Backup,
        )

        // Gera ids de notificação únicos no processo (evita sobrescrever notificações de evento).
        private val idCounter = AtomicInteger(1000)
    }
}
