package com.jonathaxs.gymnutshell.core.domain

/**
 * Sistema de medida — porte (MVP) de MeasurementSystem (iOS).
 * Por ora só Metric e US (UK/stones fica pra depois). Afeta a unidade de exibição da água
 * (ml ↔ fl oz); o armazenamento interno é sempre métrico (ml).
 */
enum class MeasurementSystem(val rawValue: String) {
    Metric("metric"),
    Us("us");

    companion object {
        fun fromRaw(raw: String?): MeasurementSystem = entries.firstOrNull { it.rawValue == raw } ?: Metric
    }
}
