package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.jonathaxs.gymnutshell.core.domain.GoalConfig
import com.jonathaxs.gymnutshell.core.domain.GoalOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Persiste a configuração da página de Goals — porte unificado dos stores do iOS que eram UserDefaults:
 * ordem das metas fixas (GoalOrderStore), ordem unificada de categorias (UnifiedCategoryOrderStore),
 * metas removidas (RemovedItemsStore), estado de colapso na Settings e os overrides de valor/incremento.
 * Reutiliza o mesmo DataStore "settings" (Preferences.kt).
 */
class GoalConfigRepository(private val context: Context) {

    private val fixedOrderKey = stringPreferencesKey("tracking.fixedOrder")
    private val categoryOrderKey = stringPreferencesKey("goal.category.unifiedOrder")
    private val removedKey = stringSetPreferencesKey("app.removedBuiltinItems")
    private val collapsedKey = stringSetPreferencesKey("settings.goal.collapsed")

    private val data = context.appPreferences.data

    // MARK: - Ordem das metas fixas

    /** Ordem das 10 metas fixas; cai pro padrão se nada salvo ou se o conjunto não bater (integridade). */
    val fixedOrder: Flow<List<String>> = data.map { prefs ->
        val stored = prefs[fixedOrderKey]?.split(",")?.filter { it.isNotEmpty() }
        if (stored != null && stored.toSet() == GoalOrder.DEFAULT.toSet()) stored else GoalOrder.DEFAULT
    }

    suspend fun saveFixedOrder(order: List<String>) {
        if (order.toSet() != GoalOrder.DEFAULT.toSet()) return
        context.appPreferences.edit { it[fixedOrderKey] = order.joinToString(",") }
    }

    // MARK: - Ordem unificada de categorias (ids "builtin:<raw>" / "custom:<id>")

    /** Ids da ordem de categorias na sequência salva; vazio = usar a ordem padrão (resolvida na UI). */
    val categoryOrderIds: Flow<List<String>> = data.map { prefs ->
        prefs[categoryOrderKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun saveCategoryOrder(ids: List<String>) {
        context.appPreferences.edit { it[categoryOrderKey] = ids.joinToString(",") }
    }

    // MARK: - Metas fixas removidas

    val removedKeys: Flow<Set<String>> = data.map { it[removedKey] ?: emptySet() }

    suspend fun remove(key: String) {
        if (key !in GoalOrder.REMOVABLE_KEYS) return
        context.appPreferences.edit { prefs ->
            prefs[removedKey] = (prefs[removedKey] ?: emptySet()) + key
        }
    }

    suspend fun restore(key: String) {
        context.appPreferences.edit { prefs ->
            prefs[removedKey] = (prefs[removedKey] ?: emptySet()) - key
        }
    }

    // MARK: - Colapso de categorias na Settings (separado do colapso da Today)

    val collapsedCategories: Flow<Set<String>> = data.map { it[collapsedKey] ?: emptySet() }

    suspend fun toggleCollapsed(categoryId: String) {
        context.appPreferences.edit { prefs ->
            val current = prefs[collapsedKey] ?: emptySet()
            prefs[collapsedKey] =
                if (categoryId in current) current - categoryId else current + categoryId
        }
    }

    // MARK: - Overrides de valor e incremento por meta fixa

    /** Valores editados por meta (chave = goal key); só conta override quando > 0 (espelha o iOS). */
    val valueOverrides: Flow<Map<String, Int>> = data.map { prefs ->
        GoalOrder.DEFAULT.mapNotNull { key ->
            prefs[intPreferencesKey(key)]?.takeIf { it > 0 }?.let { key to it }
        }.toMap()
    }

    /** Incrementos editados por meta (chave = "<goal key>.increment"); só conta quando > 0. */
    val incrementOverrides: Flow<Map<String, Int>> = data.map { prefs ->
        GoalOrder.DEFAULT.mapNotNull { key ->
            prefs[intPreferencesKey("$key.increment")]?.takeIf { it > 0 }?.let { key to it }
        }.toMap()
    }

    suspend fun setGoalValue(key: String, value: Int) {
        context.appPreferences.edit { it[intPreferencesKey(key)] = value }
    }

    suspend fun setGoalIncrement(key: String, increment: Int) {
        context.appPreferences.edit { it[intPreferencesKey("$key.increment")] = increment }
    }

    // MARK: - Config agregada

    /** Junta ordem + removidos + overrides numa única [GoalConfig] pros consumidores (Today, widget, …). */
    val goalConfig: Flow<GoalConfig> =
        combine(fixedOrder, removedKeys, valueOverrides, incrementOverrides) { order, removed, values, increments ->
            GoalConfig(
                fixedOrder = order,
                removedKeys = removed,
                valueOverrides = values,
                incrementOverrides = increments,
            )
        }
}
