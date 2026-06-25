package com.jonathaxs.gymnutshell.ui.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategoryRepository
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.DailyRecord
import com.jonathaxs.gymnutshell.core.data.DailyRecordRepository
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.AppDateFormatters
import com.jonathaxs.gymnutshell.core.domain.AppTheme
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.CategoryItem
import com.jonathaxs.gymnutshell.core.domain.DailyAchievement
import com.jonathaxs.gymnutshell.core.domain.DailyRecordCodec
import com.jonathaxs.gymnutshell.core.domain.DailyRecordFactory
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import com.jonathaxs.gymnutshell.core.domain.GoalConfig
import com.jonathaxs.gymnutshell.core.domain.GoalsCalculator
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UnifiedCategoryOrder
import com.jonathaxs.gymnutshell.core.domain.UnitConverter
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import com.jonathaxs.gymnutshell.ui.today.TodayCategoryUi
import com.jonathaxs.gymnutshell.ui.today.TodayGoalUi
import com.jonathaxs.gymnutshell.ui.today.categoryTitleRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.roundToInt

/** Estado da tela de edição de um DailyRecord — espelha o cabeçalho + metas da EditTodayView (iOS). */
data class EditRecordUiState(
    val loaded: Boolean = false,
    val dateLabel: String = "",
    val percent: Int = 0,
    val tierEmoji: String = "🐓",
    /** Nível do tier (1–4) recalculado ao vivo conforme o usuário edita. */
    val tierLevel: Int = 1,
    val sections: List<TodayCategoryUi> = emptyList(),
    val uncategorizedGoals: List<TodayGoalUi> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
)

/**
 * ViewModel da edição de conquista — porte da EditTodayView (iOS), aqui como página.
 * Semeia os intakes a partir do DailyRecord salvo, recalcula %/tier ao vivo e, ao salvar,
 * reconstrói o registro pela mesma fábrica usada na virada do dia (DailyRecordFactory.build).
 */
class EditRecordViewModel(app: Application) : AndroidViewModel(app) {

    private val recordRepo = DailyRecordRepository(app.applicationContext)
    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)
    private val customCategoryRepo = CustomGoalCategoryRepository(app.applicationContext)
    private val goalConfigRepo = GoalConfigRepository(app.applicationContext)

    private val _uiState = MutableStateFlow(EditRecordUiState())
    val uiState: StateFlow<EditRecordUiState> = _uiState.asStateFlow()

    // Contexto carregado uma vez no load(); as edições mexem só no rascunho de intakes/descanso.
    private var baseRecord: DailyRecord? = null
    private var goals: GoalsCalculator.Result? = null
    private var theme: AppTheme = AppTheme.Default
    private var measurement: MeasurementSystem = MeasurementSystem.Metric
    private var customGoals: List<CustomGoal> = emptyList()
    private var customCategories: List<CustomGoalCategory> = emptyList()
    private var config: GoalConfig = GoalConfig()
    private var categoryOrderIds: List<String> = emptyList()
    private var accentArgb: Long = 0xFF007AFF

    // Rascunho sempre em unidade métrica (a conversão p/ fl oz é só de exibição, como na Today).
    private val draftIntakes = mutableMapOf<String, Int>()
    private val draftRestDays = mutableSetOf<String>()
    // Categorias recolhidas (estado local da edição; começa tudo expandido). Chave = id da seção.
    private val collapsedCategories = mutableSetOf<String>()

    /** Carrega o registro do dia e semeia o rascunho de edição. */
    fun load(epochDay: Long) {
        viewModelScope.launch {
            val record = recordRepo.findByDate(epochDay) ?: return@launch
            val profile = profileRepo.profile.first()
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
            goals = GoalsProvider.goals(effective)
            theme = settingsRepo.theme.first()
            measurement = settingsRepo.measurementSystem.first()
            customGoals = customGoalRepo.all()
            customCategories = customCategoryRepo.all()
            config = goalConfigRepo.goalConfig.first()
            categoryOrderIds = goalConfigRepo.categoryOrderIds.first()
            accentArgb = settingsRepo.accentColor.first().argb
            baseRecord = record

            val stored = DailyRecordCodec.decodeIntMap(record.customValues)
            val res = goals!!
            draftIntakes.clear()
            draftIntakes["tracking.water"] = record.water
            draftIntakes["tracking.protein"] = record.protein
            draftIntakes["tracking.carbs"] = record.carbs
            draftIntakes["tracking.goodFat"] = record.goodFat
            draftIntakes["tracking.fiber"] = record.fiber
            draftIntakes["tracking.sleep"] = record.sleep
            draftIntakes["tracking.calories"] = stored["tracking.calories"] ?: 0
            draftIntakes["tracking.creatine"] = stored["tracking.creatine"] ?: 0
            // Treino/cardio: usa o valor salvo; em registros antigos (só booleano) cai pro alvo quando marcado.
            draftIntakes["tracking.workout"] =
                stored["tracking.workout"] ?: if (record.didWorkout) res.workout else 0
            draftIntakes["tracking.cardio"] =
                stored["tracking.cardio"] ?: if (record.didCardio) res.cardio else 0
            customGoals.forEach { draftIntakes[it.intakeKey] = stored[it.intakeKey] ?: 0 }

            draftRestDays.clear()
            if (record.workoutRestDay) draftRestDays += "tracking.workout"
            if (record.cardioRestDay) draftRestDays += "tracking.cardio"
            DailyRecordCodec.decodeBoolMap(record.customRestDays).filterValues { it }.keys.forEach { draftRestDays += it }

            recompute()
        }
    }

    /** Define o valor de uma meta (vindo do slider). Água em fl oz é convertida pra ml ao guardar. */
    fun setIntake(goal: TodayGoalUi, displayValue: Int) {
        val clamped = displayValue.coerceIn(0, goal.target)
        val toStore = if (goal.unitIsFlOz) UnitConverter.flOzToMl(clamped.toDouble()).roundToInt() else clamped
        draftIntakes[goal.key] = toStore
        recompute()
    }

    fun toggleRestDay(goal: TodayGoalUi) {
        if (goal.key in draftRestDays) draftRestDays -= goal.key else draftRestDays += goal.key
        recompute()
    }

    /** Recolhe/expande uma categoria pela id da seção (porte do cabeçalho recolhível da Today). */
    fun toggleCategory(id: String) {
        if (id in collapsedCategories) collapsedCategories -= id else collapsedCategories += id
        recompute()
    }

    /** Reconstrói o registro pela fábrica e persiste; chama `onDone` ao concluir. */
    fun save(onDone: () -> Unit) {
        val res = goals ?: return run { onDone() }
        val base = baseRecord ?: return run { onDone() }
        viewModelScope.launch {
            val rebuilt = DailyRecordFactory.build(
                epochDay = base.date,
                intakes = draftIntakes.toMap(),
                result = res,
                theme = theme,
                restDays = draftRestDays.toSet(),
                customGoals = customGoals,
                config = config,
            )
            recordRepo.upsert(rebuilt)
            onDone()
        }
    }

    /** Monta a lista de metas (built-in + custom) a partir do rascunho — mesma regra da TodayViewModel. */
    private fun recompute() {
        val res = goals ?: return

        fun supportsRest(category: GoalCategory?, customCategoryId: String?): Boolean =
            if (customCategoryId != null) {
                customCategories.firstOrNull { it.id == customCategoryId }?.supportsRestDay ?: false
            } else {
                category == GoalCategory.Treino
            }

        // Metas fixas ativas (ordem/removidas/overrides), iguais à Today.
        val builtinGoals = BuiltInGoals.active(res, config).map { g ->
            val category = GoalCategory.defaultCategory(g.key)
            val waterUs = g.key == "tracking.water" && measurement == MeasurementSystem.Us
            val storedMl = draftIntakes[g.key] ?: 0
            TodayGoalUi(
                key = g.key,
                emoji = g.emoji,
                unit = if (waterUs) "fl oz" else g.unit,
                increment = if (waterUs) 8 else g.increment,
                intake = if (waterUs) UnitConverter.mlToFlOz(storedMl.toDouble()).roundToInt() else storedMl,
                target = if (waterUs) UnitConverter.mlToFlOz(g.target.toDouble()).roundToInt() else g.target,
                isRestDay = g.key in draftRestDays,
                supportsRestDay = category == GoalCategory.Treino,
                category = category,
                unitIsFlOz = waterUs,
            )
        }
        val customGoalsUi = customGoals.map { c ->
            val category = GoalCategory.fromRaw(c.categoryRaw)
            TodayGoalUi(
                key = c.intakeKey, emoji = c.emoji, unit = c.unit, increment = c.increment,
                intake = draftIntakes[c.intakeKey] ?: 0, target = c.target,
                isRestDay = c.intakeKey in draftRestDays,
                supportsRestDay = supportsRest(category, c.customCategoryId),
                category = category,
                customCategoryId = c.customCategoryId,
                title = c.name,
            )
        }
        val allGoals = builtinGoals + customGoalsUi

        val orderedCategories = UnifiedCategoryOrder.resolve(categoryOrderIds, customCategories)
        val sections = orderedCategories.mapNotNull { item ->
            when (item) {
                is CategoryItem.Builtin -> {
                    val goalsInCat = builtinGoals.filter { it.category == item.category } +
                        customGoalsUi.filter { it.category == item.category && it.customCategoryId == null }
                    if (goalsInCat.isEmpty()) {
                        null
                    } else {
                        TodayCategoryUi(item.id, categoryTitleRes(item.category), null, goalsInCat, item.id in collapsedCategories)
                    }
                }
                is CategoryItem.Custom -> {
                    val goalsInCat = customGoalsUi.filter { it.customCategoryId == item.category.id }
                    if (goalsInCat.isEmpty()) {
                        null
                    } else {
                        TodayCategoryUi(item.id, null, item.category.name, goalsInCat, item.id in collapsedCategories)
                    }
                }
            }
        }
        val uncategorized = customGoalsUi.filter { it.category == null && it.customCategoryId == null }

        val avg = if (allGoals.isEmpty()) 0.0 else allGoals.sumOf { it.progress } / allGoals.size
        val tier = DailyAchievement.from(avg)

        _uiState.value = EditRecordUiState(
            loaded = true,
            dateLabel = AppDateFormatters.longDate(LocalDate.ofEpochDay(baseRecord?.date ?: 0L)),
            percent = floor(avg * 100).toInt(),
            tierEmoji = theme.emoji(tier),
            tierLevel = tier.ordinal + 1,
            sections = sections,
            uncategorizedGoals = uncategorized,
            accentArgb = accentArgb,
        )
    }

    companion object {
        // Mesmo perfil-demo da TodayViewModel até a tela de Welcome existir.
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
    }
}
