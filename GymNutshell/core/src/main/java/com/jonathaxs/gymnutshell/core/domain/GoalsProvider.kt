package com.jonathaxs.gymnutshell.core.domain

/**
 * Ponte entre o perfil salvo e as metas calculadas — alimenta a TodayView.
 * Mantém a UI/ViewModel desacoplados das fórmulas: pede o perfil, recebe as metas do dia.
 */
object GoalsProvider {

    fun goals(profile: Profile): GoalsCalculator.Result =
        GoalsCalculator.calculate(
            weightKg = profile.weightKg,
            heightCm = profile.heightCm,
            age = profile.age,
            sex = profile.sex,
            goal = profile.goal,
        )
}
