package com.jonathaxs.gymnutshell.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalsCalculatorTest {

    @Test
    fun `perfil masculino em manutencao bate o resultado completo`() {
        // BMR = 800 + 1125 - 150 + 5 = 1780; TDEE = 2759; manutenção = 2759 → 2750 (passo 50)
        val result = GoalsCalculator.calculate(
            weightKg = 80.0, heightCm = 180, age = 30, sex = "male", goal = UserGoal.Maintenance,
        )
        val expected = GoalsCalculator.Result(
            calories = 2750,
            water = 2750,   // 80*35=2800 → 2750
            protein = 160,  // 80*2.0
            carbs = 280,    // 80*3.5
            goodFat = 70,   // 80*0.9=72 → 70
            fiber = 39,     // 2750/1000*14=38.5 → 39
            sleep = 7,
            creatine = 4,   // 80*0.05
            workout = 50,
            cardio = 15,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `cutting reduz calorias e aumenta proteina`() {
        val r = GoalsCalculator.calculate(80.0, 180, 30, "male", UserGoal.Cutting)
        assertEquals(2200, r.calories) // 2759*0.80=2207.2 → 2200
        assertEquals(175, r.protein)   // 80*2.2=176 → 175
    }

    @Test
    fun `perfil feminino e fallback de altura e idade ausentes`() {
        // h=170, a=30, sexConstant=-161 → BMR=1351.5; TDEE=2094.825; manutenção → 2100
        val r = GoalsCalculator.calculate(60.0, heightCm = 0, age = 0, sex = "female", goal = UserGoal.Maintenance)
        assertEquals(2100, r.calories)
    }

    @Test
    fun `creatina respeita piso de 3 e peso zero usa default`() {
        assertEquals(3, GoalsCalculator.calculate(40.0, 170, 30, "male", UserGoal.Maintenance).creatine) // 40*0.05=2 → 3
        assertEquals(DefaultGoals.CREATINE, GoalsCalculator.calculate(0.0, 170, 30, "male", UserGoal.Maintenance).creatine)
    }
}
