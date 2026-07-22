package com.jonathaxs.gymnutshell.util

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

/**
 * Troca de idioma por-app sem AppCompat — porte da opção de idioma do iOS.
 *
 * - **Android 13+**: usa o [LocaleManager] nativo. Ele persiste a escolha, recria a Activity sozinho
 *   e reflete no seletor de idioma das Configurações do sistema (que o `localeConfig` no manifest liga).
 * - **Android 12 e abaixo**: não existe seletor no sistema. Guardamos a tag numa SharedPreferences,
 *   aplicamos o locale no [wrap] (chamado do `attachBaseContext` da MainActivity) e recriamos a Activity.
 */
object AppLocales {

    /** Opções na ordem do seletor. "" = seguir o idioma do sistema. */
    val OPTIONS = listOf("", "en", "pt-BR")

    private const val PREFS = "app_locale"
    private const val KEY_TAG = "language_tag"

    private val usesFramework: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** Tag do idioma escolhido hoje ("" = padrão do sistema). */
    fun current(context: Context): String =
        if (usesFramework) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeIf { !it.isEmpty }
                ?.get(0)?.toLanguageTag()
                ?: ""
        } else {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, "").orEmpty()
        }

    /** Aplica a escolha. No 13+ o framework recria a Activity; abaixo disso, recriamos na mão. */
    fun apply(context: Context, tag: String) {
        if (usesFramework) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                if (tag.isEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        } else {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_TAG, tag) }
            (context as? Activity)?.recreate()
        }
    }

    /** Envolve o base context com o locale salvo — chamado no `attachBaseContext` (só relevante < 33). */
    fun wrap(base: Context): Context {
        if (usesFramework) return base
        val tag = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, "").orEmpty()
        if (tag.isEmpty()) return base
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return base.createConfigurationContext(config)
    }
}
