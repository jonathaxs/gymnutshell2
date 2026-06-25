package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.R
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategoryRepository
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.ProfileRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.BuiltInGoals
import com.jonathaxs.gymnutshell.core.domain.CategoryItem
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import com.jonathaxs.gymnutshell.core.domain.GoalOrder
import com.jonathaxs.gymnutshell.core.domain.GoalsProvider
import com.jonathaxs.gymnutshell.core.domain.MeasurementSystem
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UnifiedCategoryOrder
import com.jonathaxs.gymnutshell.core.domain.UnitConverter
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Linha de uma meta fixa no hub de Goals. */
data class FixedRowUi(
    val key: String,
    val emoji: String,
    @StringRes val titleRes: Int,
    val valueDisplay: String,
    @StringRes val badgeRes: Int,
    val removed: Boolean,
    val removable: Boolean,
)

/** Linha de uma meta personalizada no hub de Goals. */
data class CustomRowUi(
    val id: Long,
    val emoji: String,
    val title: String,
    val valueDisplay: String,
)

/** Seção de uma categoria no hub: metas fixas (reordenáveis entre si) + metas personalizadas (idem). */
data class GoalSectionUi(
    val id: String,
    @StringRes val titleRes: Int?,
    val title: String?,
    val collapsed: Boolean,
    val fixedRows: List<FixedRowUi>,
    val customRows: List<CustomRowUi>,
)

/** Estado do hub de Goals. */
data class GoalsUiState(
    val sections: List<GoalSectionUi> = emptyList(),
    val uncategorized: List<CustomRowUi> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
)

/**
 * ViewModel do hub de Goals — porte de TrackingGoalsSettingsView (iOS).
 * Junta perfil → metas calculadas + GoalConfig (ordem/removidos/overrides) + metas e categorias
 * personalizadas + colapso da Settings, e monta as seções agrupadas na ordem unificada de categorias.
 */
class GoalsViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = ProfileRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)
    private val goalConfigRepo = GoalConfigRepository(app.applicationContext)
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)
    private val customCategoryRepo = CustomGoalCategoryRepository(app.applicationContext)

    val uiState: StateFlow<GoalsUiState> =
        combine(
            combine(profileRepo.profile, customGoalRepo.goals, customCategoryRepo.categories) { p, g, c ->
                Triple(p, g, c)
            },
            combine(
                goalConfigRepo.goalConfig,
                goalConfigRepo.categoryOrderIds,
                goalConfigRepo.collapsedCategories,
            ) { config, orderIds, collapsed -> Triple(config, orderIds, collapsed) },
            combine(settingsRepo.measurementSystem, settingsRepo.accentColor) { m, a -> m to a },
        ) { (profile, customGoals, customCategories), (config, orderIds, collapsed), (measurement, accent) ->
            val effective = if (profile.weightKg <= 0.0) DEMO_PROFILE else profile
            val result = GoalsProvider.goals(effective)
            val base = BuiltInGoals.forResult(result).associateBy { it.key }
            // Ordem das metas fixas (mostra TODAS, inclusive removidas, pra exibir a linha acinzentada).
            val orderedKeys = config.fixedOrder + base.keys.filter { it !in config.fixedOrder }

            // Monta uma linha fixa pra cada chave, ciente da água em US e dos overrides de valor.
            fun fixedRow(key: String): FixedRowUi? {
                val g = base[key] ?: return null
                val rawValue = config.valueOverrides[key] ?: g.target
                val waterUs = key == "tracking.water" && measurement == MeasurementSystem.Us
                val shownValue = if (waterUs) UnitConverter.mlToFlOz(rawValue.toDouble()).roundToInt() else rawValue
                val unit = if (waterUs) "fl oz" else g.unit
                val removable = key in GoalOrder.REMOVABLE_KEYS
                return FixedRowUi(
                    key = key,
                    emoji = g.emoji,
                    titleRes = goalTitleRes(key),
                    valueDisplay = "$shownValue $unit",
                    badgeRes = if (removable) R.string.goal_badge_recommended else R.string.goal_badge_standard,
                    removed = key in config.removedKeys,
                    removable = removable,
                )
            }

            fun customRow(id: Long): CustomRowUi? =
                customGoals.firstOrNull { it.id == id }?.let { c ->
                    CustomRowUi(c.id, c.emoji, c.name, "${c.target} ${c.unit}")
                }

            val orderedCategories = UnifiedCategoryOrder.resolve(orderIds, customCategories)
            val sections = orderedCategories.mapNotNull { item ->
                val fixedRows: List<FixedRowUi>
                val customRows: List<CustomRowUi>
                val titleRes: Int?
                val title: String?
                when (item) {
                    is CategoryItem.Builtin -> {
                        titleRes = categoryTitleRes(item.category)
                        title = null
                        fixedRows = orderedKeys
                            .filter { GoalCategory.defaultCategory(it) == item.category }
                            .mapNotNull { fixedRow(it) }
                        customRows = customGoals
                            .filter { it.categoryRaw == item.category.rawValue && it.customCategoryId == null }
                            .mapNotNull { customRow(it.id) }
                    }
                    is CategoryItem.Custom -> {
                        titleRes = null
                        title = item.category.name
                        fixedRows = emptyList()
                        customRows = customGoals
                            .filter { it.customCategoryId == item.category.id }
                            .mapNotNull { customRow(it.id) }
                    }
                }
                if (fixedRows.isEmpty() && customRows.isEmpty()) return@mapNotNull null
                GoalSectionUi(item.id, titleRes, title, item.id in collapsed, fixedRows, customRows)
            }

            // Metas personalizadas sem categoria (compat. com metas antigas sem categoria definida).
            val uncategorized = customGoals
                .filter { it.categoryRaw == null && it.customCategoryId == null }
                .mapNotNull { customRow(it.id) }

            GoalsUiState(sections = sections, uncategorized = uncategorized, accentArgb = accent.argb)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    // MARK: - Ações

    fun toggleCategory(id: String) {
        viewModelScope.launch { goalConfigRepo.toggleCollapsed(id) }
    }

    fun removeFixed(key: String) {
        viewModelScope.launch { goalConfigRepo.remove(key) }
    }

    fun restoreFixed(key: String) {
        viewModelScope.launch { goalConfigRepo.restore(key) }
    }

    /** Move uma meta fixa pra cima/baixo dentro da própria categoria (porte de moveGoalsWithinCategory do iOS). */
    fun moveFixed(key: String, up: Boolean) {
        val category = GoalCategory.defaultCategory(key) ?: return
        viewModelScope.launch {
            val order = goalConfigRepo.fixedOrder.first()
            val subset = order.filter { GoalCategory.defaultCategory(it) == category }
            val i = subset.indexOf(key)
            val j = if (up) i - 1 else i + 1
            if (i < 0 || j !in subset.indices) return@launch
            val newSubset = subset.toMutableList().also { it[i] = subset[j]; it[j] = subset[i] }
            val indices = subset.map { order.indexOf(it) }
            val newOrder = order.toMutableList()
            indices.forEachIndexed { k, idx -> newOrder[idx] = newSubset[k] }
            goalConfigRepo.saveFixedOrder(newOrder)
        }
    }

    /**
     * Move uma meta personalizada pra cima/baixo entre as irmãs da mesma categoria.
     * `siblingIds` são os ids das metas custom da seção na ordem exibida.
     */
    fun moveCustom(goalId: Long, up: Boolean, siblingIds: List<Long>) {
        viewModelScope.launch {
            val i = siblingIds.indexOf(goalId)
            val j = if (up) i - 1 else i + 1
            if (i < 0 || j !in siblingIds.indices) return@launch
            val otherId = siblingIds[j]
            val all = customGoalRepo.all().map { it.id }.toMutableList()
            val pi = all.indexOf(goalId)
            val pj = all.indexOf(otherId)
            if (pi < 0 || pj < 0) return@launch
            all[pi] = otherId
            all[pj] = goalId
            customGoalRepo.reorder(all)
        }
    }

    fun deleteCustom(goalId: Long) {
        viewModelScope.launch {
            customGoalRepo.all().firstOrNull { it.id == goalId }?.let { customGoalRepo.delete(it) }
        }
    }

    companion object {
        // Perfil-demo temporário até a tela de Welcome existir (mesmo da TodayViewModel).
        private val DEMO_PROFILE = Profile(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )

        /** Categoria fixa → título localizado (mesmo mapeamento da TodayScreen). */
        @StringRes
        fun categoryTitleRes(category: GoalCategory): Int = when (category) {
            GoalCategory.Essencial -> R.string.category_essencial
            GoalCategory.Nutricao -> R.string.category_nutricao
            GoalCategory.Treino -> R.string.category_treino
            GoalCategory.Suplemento -> R.string.category_suplemento
        }

        /** Chave da meta fixa → título localizado (reaproveita as strings da Today). */
        @StringRes
        fun goalTitleRes(key: String): Int = when (key) {
            "tracking.workout" -> R.string.today_workout
            "tracking.cardio" -> R.string.today_cardio
            "tracking.sleep" -> R.string.today_sleep
            "tracking.water" -> R.string.today_water
            "tracking.calories" -> R.string.today_calories
            "tracking.protein" -> R.string.today_protein
            "tracking.carbs" -> R.string.today_carbs
            "tracking.goodFat" -> R.string.today_good_fat
            "tracking.fiber" -> R.string.today_fiber
            "tracking.creatine" -> R.string.today_creatine
            else -> R.string.app_name
        }
    }
}
