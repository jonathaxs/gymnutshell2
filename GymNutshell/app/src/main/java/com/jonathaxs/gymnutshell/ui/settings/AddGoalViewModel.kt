package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoal
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategoryRepository
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.GoalCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/** Seleção de categoria do formulário: uma categoria fixa ou uma criada pelo usuário. */
sealed interface CategorySelection {
    data class Builtin(val category: GoalCategory) : CategorySelection
    data class Custom(val id: String) : CategorySelection
}

/** Dados carregados pro formulário (validação de duplicados + pré-preenchimento na edição). */
data class AddGoalData(
    val loaded: Boolean = false,
    val existingGoals: List<CustomGoal> = emptyList(),
    val customCategories: List<CustomGoalCategory> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
    val editing: CustomGoal? = null,
)

/**
 * ViewModel do formulário de meta personalizada — porte de AddTrackingGoalView (iOS).
 * Carrega metas/categorias existentes (pra checar duplicados) e persiste no salvar (cria categoria nova se preciso).
 */
class AddGoalViewModel(app: Application) : AndroidViewModel(app) {

    private val customGoalRepo = CustomGoalRepository(app.applicationContext)
    private val customCategoryRepo = CustomGoalCategoryRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    private val _data = MutableStateFlow(AddGoalData())
    val data: StateFlow<AddGoalData> = _data.asStateFlow()

    /** Carrega os dados uma vez; `goalId` != null pré-preenche o formulário pra edição. */
    fun start(goalId: Long?) {
        if (_data.value.loaded) return
        viewModelScope.launch {
            val goals = customGoalRepo.all()
            _data.value = AddGoalData(
                loaded = true,
                existingGoals = goals,
                customCategories = customCategoryRepo.all(),
                accentArgb = settingsRepo.accentColor.first().argb,
                editing = goalId?.let { id -> goals.firstOrNull { it.id == id } },
            )
        }
    }

    /** Salva (cria/edita) a meta; se `newCategory` != null, cria a categoria antes e associa a meta a ela. */
    fun save(
        emoji: String,
        name: String,
        unit: String,
        target: Int,
        increment: Int,
        selection: CategorySelection,
        newCategory: Pair<String, Boolean>?,
        editingId: Long?,
    ) {
        viewModelScope.launch {
            var resolved = selection
            newCategory?.let { (catName, restDay) ->
                val category = CustomGoalCategory(
                    id = UUID.randomUUID().toString(),
                    name = catName.trim(),
                    supportsRestDay = restDay,
                )
                customCategoryRepo.upsert(category)
                resolved = CategorySelection.Custom(category.id)
            }
            val categoryRaw = (resolved as? CategorySelection.Builtin)?.category?.rawValue
            val customCategoryId = (resolved as? CategorySelection.Custom)?.id

            val editing = editingId?.let { id -> customGoalRepo.all().firstOrNull { it.id == id } }
            if (editing != null) {
                customGoalRepo.update(
                    editing.copy(
                        emoji = emoji.trim(),
                        name = name.trim(),
                        unit = unit.trim(),
                        target = target,
                        increment = increment,
                        categoryRaw = categoryRaw,
                        customCategoryId = customCategoryId,
                    ),
                )
            } else {
                customGoalRepo.add(
                    CustomGoal(
                        emoji = emoji.trim(),
                        name = name.trim(),
                        unit = unit.trim(),
                        target = target,
                        increment = increment,
                        categoryRaw = categoryRaw,
                        customCategoryId = customCategoryId,
                    ),
                )
            }
        }
    }

    companion object {
        // Emojis e nomes das metas fixas — bloqueiam duplicados (porte dos reservedEmojis/reservedNames do iOS).
        val RESERVED_EMOJIS: Set<String> = setOf("🏋️", "🏃", "💤", "💧", "🍗", "🍞", "🧈", "🌾", "🧪")
        val RESERVED_NAMES: Set<String> = setOf(
            "workout", "cardio", "sleep", "water", "protein", "carbs", "fats", "fiber", "creatine",
            "treino", "sono", "água", "proteína", "carboidratos", "gorduras", "fibra", "creatina",
        )
    }
}
