package com.jonathaxs.gymnutshell.core.domain

/**
 * Preferência de travamento de orientação escolhida pelo usuário — porte de AppOrientation (iOS).
 *
 * Só faz sentido no celular: o tablet sempre aceita ambas as orientações (a tela de
 * Orientação nem aparece nas Settings dele). A camada de UI (:app) é quem decide o
 * default por dispositivo e aplica a máscara via `Activity.requestedOrientation`.
 */
enum class AppOrientation(val rawValue: String) {
    /** Trava o app na vertical (default do celular). */
    Portrait("portrait"),

    /** Aceita vertical e horizontal (default do tablet). */
    Both("both");

    companion object {
        /** Chave de persistência — espelha AppOrientation.storageKey do iOS. */
        const val STORAGE_KEY = "app.orientation.lock"

        /** Default antes de o usuário escolher: vertical (celular). O tablet ignora e fica livre. */
        val Default = Portrait

        fun fromRaw(raw: String?): AppOrientation =
            entries.firstOrNull { it.rawValue == raw } ?: Default
    }
}
