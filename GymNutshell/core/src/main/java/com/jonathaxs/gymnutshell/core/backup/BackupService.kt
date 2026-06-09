package com.jonathaxs.gymnutshell.core.backup

import android.content.Context
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.DailyRecord
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.HealthPreferencesRepository
import com.jonathaxs.gymnutshell.core.data.NotificationPreferencesRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.core.theme.AccentColor
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Monta e aplica um [BackupPayload] a partir do estado do app — porte do BackupManager (iOS).
 * Lê de/escreve nos repositórios do `:core` (perfil, ajustes, registros, metas, prefs).
 * A leitura/escrita de arquivo (Storage Access Framework) fica na camada de UI (5C-3).
 */
class BackupService(context: Context) {

    private val profileRepo = ProfileRepository(context)
    private val settingsRepo = SettingsRepository(context)
    private val recordRepo = DailyRecordRepository(context)
    private val customGoalRepo = CustomGoalRepository(context)
    private val notifPrefs = NotificationPreferencesRepository(context)
    private val healthPrefs = HealthPreferencesRepository(context)

    // Json só pra (de)serializar o mapa de valores custom guardado em DailyRecord.customValues.
    private val mapJson = Json { ignoreUnknownKeys = true }

    // MARK: - Export

    /** Estado completo do app como JSON pronto pra gravar em arquivo. */
    suspend fun exportJson(): String = BackupCodec.encode(buildPayload())

    /** Monta o payload lendo todos os repositórios. */
    suspend fun buildPayload(): BackupPayload {
        val profile = profileRepo.profile.first()
        val measurement = settingsRepo.measurementSystem.first()
        val theme = settingsRepo.theme.first()
        val accent = settingsRepo.accentColor.first()
        val goals = GoalsProvider.goals(profile)

        return BackupPayload(
            version = BackupCodec.CURRENT_VERSION,
            exportedAt = Instant.now().toString(),
            profile = ProfileSnapshot(
                name = profile.name,
                height = profile.heightCm,
                weight = profile.weightKg,
                age = profile.age,
                sex = profile.sex,
                userGoal = profile.goal.rawValue,
                measurementSystem = measurement.rawValue,
            ),
            goals = GoalsSnapshot(
                calories = goals.calories, sleep = goals.sleep, water = goals.water,
                protein = goals.protein, carbs = goals.carbs, goodFat = goals.goodFat, fiber = goals.fiber,
            ),
            goalsOrder = emptyList(), // Android ainda não reordena metas → vazio (iOS lê e ignora).
            customGoals = customGoalRepo.all().map { it.toSnapshot() },
            dailyRecords = recordRepo.allRecords().map { it.toSnapshot() },
            appearance = AppearanceSnapshot(theme = theme.rawValue, accentColor = accent.name),
            preferences = PreferencesSnapshot(
                autoWorkoutCheckin = healthPrefs.autoWorkoutCheckin(),
                notificationEnabled = notifPrefs.enabledSnapshot().ifEmpty { null },
                notificationInterval = notifPrefs.intervalSnapshot().ifEmpty { null },
                notificationSound = notifPrefs.soundSnapshot().ifEmpty { null },
            ),
        )
    }

    // MARK: - Import

    /** Decodifica e aplica um backup completo (substitui o estado atual). */
    suspend fun importJson(text: String) = restore(BackupCodec.decode(text))

    /**
     * Aplica o payload: substitui perfil, ajustes, metas e todo o histórico.
     * O bloco `goals` é ignorado (Android recalcula a partir do perfil — não há override de alvo).
     */
    suspend fun restore(payload: BackupPayload) {
        val p = payload.profile
        profileRepo.update(
            Profile(
                name = p.name,
                weightKg = p.weight,
                heightCm = p.height,
                age = p.age,
                sex = p.sex,
                goal = UserGoal.fromRaw(p.userGoal) ?: UserGoal.Maintenance,
            ),
        )
        p.measurementSystem?.let { settingsRepo.setMeasurementSystem(MeasurementSystem.fromRaw(it)) }

        payload.appearance?.let { a ->
            settingsRepo.setTheme(AppTheme.fromRaw(a.theme))
            runCatching { AccentColor.valueOf(a.accentColor) }.getOrNull()?.let { settingsRepo.setAccentColor(it) }
        }

        customGoalRepo.replaceAll(payload.customGoals.map { it.toEntity() })
        recordRepo.replaceAll(payload.dailyRecords.map { it.toEntity() })

        payload.preferences?.let { prefs ->
            prefs.autoWorkoutCheckin?.let { healthPrefs.setAutoWorkoutCheckin(it) }
            notifPrefs.restoreFromBackup(prefs.notificationEnabled, prefs.notificationInterval, prefs.notificationSound)
        }
    }

    /** Nome de arquivo sugerido — porte de suggestedFilename (iOS). */
    fun suggestedFilename(userName: String): String {
        val sanitized = userName.lowercase().filter { it.isLetterOrDigit() }.ifEmpty { "user" }
        return "gymnutshell-$sanitized-backup-${LocalDate.now()}.json"
    }

    // MARK: - Mapeamento entidade ↔ snapshot

    private fun CustomGoal.toSnapshot() = CustomGoalSnapshot(
        id = id.toString(),
        emoji = emoji, name = name, unit = unit, goal = target, increment = increment, category = categoryRaw,
    )

    private fun CustomGoalSnapshot.toEntity() = CustomGoal(
        // id numérico (Android) é preservado; UUID do iOS não converte → 0 = Room gera um novo.
        id = id.toLongOrNull() ?: 0L,
        emoji = emoji, name = name, unit = unit, target = goal, increment = increment, categoryRaw = category,
    )

    private fun DailyRecord.toSnapshot() = RecordSnapshot(
        date = LocalDate.ofEpochDay(date).atStartOfDay(ZoneOffset.UTC).toInstant().toString(),
        water = water, protein = protein, carbs = carbs, goodFat = goodFat, fiber = fiber,
        sleep = sleep, percent = percent, achievementTitle = achievementTitle, achievementEmoji = achievementEmoji,
        points = points, didWorkout = didWorkout, didCardio = didCardio, customValues = decodeIntMap(customValues),
    )

    private fun RecordSnapshot.toEntity() = DailyRecord(
        date = parseEpochDay(date),
        water = water, protein = protein, carbs = carbs, goodFat = goodFat, fiber = fiber, sleep = sleep,
        customValues = encodeIntMap(customValues),
        didWorkout = didWorkout, didCardio = didCardio ?: false,
        percent = percent, achievementTitle = achievementTitle, achievementEmoji = achievementEmoji, points = points,
    )

    private fun decodeIntMap(text: String): Map<String, Int> =
        runCatching { mapJson.decodeFromString<Map<String, Int>>(text) }.getOrDefault(emptyMap())

    private fun encodeIntMap(map: Map<String, Int>): String = mapJson.encodeToString(map)

    /**
     * Converte a data ISO8601 do backup em epoch-day. Android↔Android é exato (gravamos 00:00Z);
     * de iOS pode dar ±1 dia em fusos extremos (o iOS serializa o instante em UTC, sem o offset original).
     */
    private fun parseEpochDay(iso: String): Long = runCatching {
        Instant.parse(iso).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
    }.getOrElse { LocalDate.parse(iso.take(10)).toEpochDay() }
}
