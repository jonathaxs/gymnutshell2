package com.jonathaxs.gymnutshell.core.domain

import com.jonathaxs.gymnutshell.core.data.CustomGoalCategory

/**
 * Configuração de metas definida pelo usuário na página de Goals — porte unificado dos stores do iOS
 * (GoalOrderStore, RemovedItemsStore e os overrides de valor/incremento em UserDefaults).
 * Passada pra [BuiltInGoals.active] pra montar a lista efetiva de metas fixas (ordem + remoção + overrides).
 */
data class GoalConfig(
    val fixedOrder: List<String> = GoalOrder.DEFAULT,
    val removedKeys: Set<String> = emptySet(),
    val valueOverrides: Map<String, Int> = emptyMap(),
    val incrementOverrides: Map<String, Int> = emptyMap(),
)

/** Ordem padrão das dez metas fixas — porte de GoalOrderStore.defaultOrder (iOS), na ordem das categorias. */
object GoalOrder {
    val DEFAULT: List<String> = listOf(
        "tracking.sleep",
        "tracking.water",
        "tracking.calories",
        "tracking.protein",
        "tracking.carbs",
        "tracking.goodFat",
        "tracking.fiber",
        "tracking.workout",
        "tracking.cardio",
        "tracking.creatine",
    )

    /** Metas opcionais que o usuário pode remover — porte de RemovedItemsStore.removableTrackingKeys (iOS). */
    val REMOVABLE_KEYS: Set<String> = setOf("tracking.goodFat", "tracking.creatine")
}

/**
 * Item da ordem de categorias: uma categoria fixa do app ou uma criada pelo usuário.
 * Porte de CategoryItem (iOS, UnifiedCategoryOrderStore).
 */
sealed interface CategoryItem {
    val id: String

    data class Builtin(val category: GoalCategory) : CategoryItem {
        override val id: String get() = "builtin:${category.rawValue}"
    }

    data class Custom(val category: CustomGoalCategory) : CategoryItem {
        override val id: String get() = "custom:${category.id}"
    }
}

/** Reconcilia a ordem salva com as categorias existentes — porte de UnifiedCategoryOrderStore.load (iOS). */
object UnifiedCategoryOrder {

    /**
     * Monta a lista ordenada de categorias (fixas + personalizadas) a partir dos ids salvos.
     * Itens conhecidos vêm na ordem salva; categorias novas (ainda sem ordem) são anexadas no fim.
     */
    fun resolve(storedIds: List<String>, customCategories: List<CustomGoalCategory>): List<CategoryItem> {
        val all: List<CategoryItem> =
            GoalCategory.entries.map { CategoryItem.Builtin(it) } +
                customCategories.map { CategoryItem.Custom(it) }
        val byId = all.associateBy { it.id }

        val result = mutableListOf<CategoryItem>()
        val used = mutableSetOf<String>()
        for (id in storedIds) {
            byId[id]?.let { result += it; used += id }
        }
        for (item in all) {
            if (item.id !in used) result += item
        }
        return result
    }
}
