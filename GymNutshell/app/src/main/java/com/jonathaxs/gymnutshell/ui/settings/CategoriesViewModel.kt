package com.jonathaxs.gymnutshell.ui.settings

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory
import com.jonathaxs.gymnutshell.core.data.CustomGoalCategoryRepository
import com.jonathaxs.gymnutshell.core.data.CustomGoalRepository
import com.jonathaxs.gymnutshell.core.data.GoalConfigRepository
import com.jonathaxs.gymnutshell.core.data.SettingsRepository
import com.jonathaxs.gymnutshell.core.domain.CategoryItem
import com.jonathaxs.gymnutshell.core.domain.UnifiedCategoryOrder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Item da lista de ordenação de categorias. */
data class CategoryItemUi(
    val id: String,
    @StringRes val titleRes: Int?,
    val title: String?,
    val custom: CustomGoalCategory?,
) {
    val isCustom: Boolean get() = custom != null
}

/** Estado da subtela de Categorias. */
data class CategoriesUiState(
    val items: List<CategoryItemUi> = emptyList(),
    val accentArgb: Long = 0xFF007AFF,
)

/**
 * ViewModel da subtela de Categorias — porte de CategoriesSettingsView (iOS).
 * Reordena a ordem unificada (fixas + personalizadas) e edita/exclui categorias personalizadas.
 */
class CategoriesViewModel(app: Application) : AndroidViewModel(app) {

    private val goalConfigRepo = GoalConfigRepository(app.applicationContext)
    private val customCategoryRepo = CustomGoalCategoryRepository(app.applicationContext)
    private val customGoalRepo = CustomGoalRepository(app.applicationContext)
    private val settingsRepo = SettingsRepository(app.applicationContext)

    val uiState: StateFlow<CategoriesUiState> =
        combine(
            goalConfigRepo.categoryOrderIds,
            customCategoryRepo.categories,
            settingsRepo.accentColor,
        ) { orderIds, customCategories, accent ->
            val items = UnifiedCategoryOrder.resolve(orderIds, customCategories).map { item ->
                when (item) {
                    is CategoryItem.Builtin -> CategoryItemUi(
                        id = item.id,
                        titleRes = GoalsViewModel.categoryTitleRes(item.category),
                        title = null,
                        custom = null,
                    )
                    is CategoryItem.Custom -> CategoryItemUi(
                        id = item.id,
                        titleRes = null,
                        title = item.category.name,
                        custom = item.category,
                    )
                }
            }
            CategoriesUiState(items = items, accentArgb = accent.argb)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    /** Move uma categoria pra cima/baixo e persiste a ordem unificada completa. */
    fun move(id: String, up: Boolean) {
        viewModelScope.launch {
            val customs = customCategoryRepo.all()
            val ids = UnifiedCategoryOrder.resolve(goalConfigRepo.categoryOrderIds.first(), customs).map { it.id }.toMutableList()
            val i = ids.indexOf(id)
            val j = if (up) i - 1 else i + 1
            if (i < 0 || j !in ids.indices) return@launch
            val tmp = ids[i]; ids[i] = ids[j]; ids[j] = tmp
            goalConfigRepo.saveCategoryOrder(ids)
        }
    }

    /** Exclui uma categoria personalizada: remove da ordem, desvincula as metas e apaga o registro. */
    fun deleteCustom(categoryId: String) {
        viewModelScope.launch {
            val customs = customCategoryRepo.all()
            val category = customs.firstOrNull { it.id == categoryId } ?: return@launch
            val ids = UnifiedCategoryOrder.resolve(goalConfigRepo.categoryOrderIds.first(), customs)
                .map { it.id }
                .filter { it != "custom:$categoryId" }
            goalConfigRepo.saveCategoryOrder(ids)
            customGoalRepo.clearCustomCategory(categoryId)
            customCategoryRepo.delete(category)
        }
    }

    /** Salva o nome + toggle de dia-off de uma categoria personalizada (sheet de edição). */
    fun saveCategory(category: CustomGoalCategory) {
        viewModelScope.launch { customCategoryRepo.upsert(category) }
    }
}
