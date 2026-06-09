package com.jonathaxs.gymnutshell.health

import android.content.Context
import androidx.annotation.StringRes
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Centraliza o acesso ao Health Connect — porte do HealthKitManager (iOS).
 * Responsável por checar a disponibilidade do provedor, guardar as permissões
 * necessárias e (nas próximas fatias) ler treinos e gravar sono.
 *
 * No minSdk 29 o provedor pode não existir: até o Android 13 é um app separado da Play Store;
 * a partir do Android 14 vem nativo no SO. Por isso tudo degrada com elegância quando indisponível.
 */
class HealthConnectManager(context: Context) {

    private val appContext = context.applicationContext

    /** Status do SDK no aparelho: disponível, indisponível ou provedor desatualizado. */
    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(appContext)

    /** true quando o Health Connect está pronto pra uso neste aparelho. */
    val isAvailable: Boolean
        get() = sdkStatus() == HealthConnectClient.SDK_AVAILABLE

    /**
     * Cliente do Health Connect, ou null quando o provedor não está disponível.
     * Recriado sob demanda (não cacheado) pra refletir o usuário instalar/atualizar o provedor.
     */
    private val client: HealthConnectClient?
        get() = if (isAvailable) HealthConnectClient.getOrCreate(appContext) else null

    // Permissões granulares (uma string por tipo de registro + ação), no estilo do Health Connect.
    private val readExercise = HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    private val writeSleep = HealthPermission.getWritePermission(SleepSessionRecord::class)

    /** Permissão pra ler treinos (auto check-in de Treino/Cardio). */
    val workoutPermissions: Set<String> = setOf(readExercise)

    /** Permissão pra gravar sono no Health Connect (sync de sono). */
    val sleepPermissions: Set<String> = setOf(writeSleep)

    /** Todas as permissões do app, pra pedir de uma vez na tela de Ajustes. */
    val allPermissions: Set<String> = workoutPermissions + sleepPermissions

    /** Conjunto de permissões já concedidas (vazio quando indisponível). */
    suspend fun grantedPermissions(): Set<String> =
        client?.permissionController?.getGrantedPermissions() ?: emptySet()

    /** true quando a permissão de leitura de treino já foi concedida. */
    suspend fun hasWorkoutPermission(): Boolean =
        grantedPermissions().containsAll(workoutPermissions)

    /** true quando a permissão de gravação de sono já foi concedida. */
    suspend fun hasSleepPermission(): Boolean =
        grantedPermissions().containsAll(sleepPermissions)

    /**
     * Lê os treinos de hoje, soma os minutos por categoria (força/cardio) e captura o nome da
     * primeira atividade de cada uma — porte do `checkTodayWorkouts` (iOS).
     * Retorna um resumo zerado quando indisponível ou sem permissão de leitura.
     */
    suspend fun readTodayWorkouts(): WorkoutSummary {
        val hc = client ?: return WorkoutSummary()
        if (!hasWorkoutPermission()) return WorkoutSummary()

        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val response = hc.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now()),
            ),
        )

        var workoutMinutes = 0
        var cardioMinutes = 0
        var workoutNameRes: Int? = null
        var cardioNameRes: Int? = null

        for (record in response.records) {
            val minutes = Duration.between(record.startTime, record.endTime).toMinutes().toInt()
            when (ExerciseClassifier.categoryOf(record.exerciseType)) {
                WorkoutCategory.Strength -> {
                    workoutMinutes += minutes
                    if (workoutNameRes == null) workoutNameRes = ExerciseClassifier.displayNameRes(record.exerciseType)
                }
                WorkoutCategory.Cardio -> {
                    cardioMinutes += minutes
                    if (cardioNameRes == null) cardioNameRes = ExerciseClassifier.displayNameRes(record.exerciseType)
                }
                null -> Unit // tipo não classificado, ignorado
            }
        }

        return WorkoutSummary(workoutMinutes, cardioMinutes, workoutNameRes, cardioNameRes)
    }

    /**
     * Grava uma sessão de sono no Health Connect (das 00h do dia até `hours` horas depois) —
     * porte mecânico do `writeSleepIfNeeded` (iOS). NÃO checa o toggle `syncSleepEnabled` nem
     * dispara notificação: isso fica a cargo de quem chama (TodayViewModel, na 5B-5).
     * Retorna true quando a gravação aconteceu; false se indisponível, sem permissão ou horas <= 0.
     */
    suspend fun writeSleep(date: LocalDate, hours: Int): Boolean {
        if (hours <= 0) return false
        val hc = client ?: return false
        if (!hasSleepPermission()) return false

        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone)
        val end = start.plusHours(hours.toLong())
        val record = SleepSessionRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            // Sono é digitado à mão pelo usuário, então marcamos como entrada manual (vinda do celular).
            metadata = Metadata.manualEntry(Device(type = Device.TYPE_PHONE)),
            title = null,
            notes = null,
            stages = emptyList(),
        )
        hc.insertRecords(listOf(record))
        return true
    }
}

/**
 * Resumo dos treinos de hoje lidos do Health Connect. Os nomes vêm como @StringRes
 * pra serem resolvidos na camada de UI/notificação (que tem o Context).
 */
data class WorkoutSummary(
    val workoutMinutes: Int = 0,
    val cardioMinutes: Int = 0,
    @param:StringRes val workoutNameRes: Int? = null,
    @param:StringRes val cardioNameRes: Int? = null,
)
