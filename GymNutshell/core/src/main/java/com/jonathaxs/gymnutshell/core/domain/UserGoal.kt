package com.jonathaxs.gymnutshell.core.domain

/**
 * Objetivo de fitness do usuário — porte de UserGoal (iOS).
 * `rawValue` é a chave persistida (DataStore), idêntica ao rawValue do enum Swift.
 */
enum class UserGoal(val rawValue: String) {
    Bulking("bulking"),
    Maintenance("maintenance"),
    Cutting("cutting");

    companion object {
        fun fromRaw(raw: String?): UserGoal? = entries.firstOrNull { it.rawValue == raw }
    }
}
