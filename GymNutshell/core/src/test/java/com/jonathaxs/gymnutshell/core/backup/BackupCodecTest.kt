package com.jonathaxs.gymnutshell.core.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Garante o round-trip do backup e a presença das chaves legadas (compat com o formato v4 do iOS). */
class BackupCodecTest {

    private fun samplePayload() = BackupPayload(
        version = BackupCodec.CURRENT_VERSION,
        exportedAt = "2026-06-08T00:00:00Z",
        profile = ProfileSnapshot(
            name = "Jonathas", height = 180, weight = 80.0, age = 30, sex = "male",
            userGoal = "maintenance", measurementSystem = "metric",
        ),
        goals = GoalsSnapshot(calories = 2900, sleep = 7, water = 3250, protein = 180, carbs = 320, goodFat = 80, fiber = 30),
        goalsOrder = emptyList(),
        customGoals = listOf(
            CustomGoalSnapshot(id = "3", emoji = "🧪", name = "Creatine", unit = "g", goal = 5, increment = 1, category = "Suplemento"),
        ),
        dailyRecords = listOf(
            RecordSnapshot(
                date = "2026-06-07T00:00:00Z", water = 3000, protein = 170, carbs = 300, goodFat = 70, fiber = 28,
                sleep = 7, percent = 92, achievementTitle = "Level 5", achievementEmoji = "🐉", points = 5,
                didWorkout = true, didCardio = false, customValues = mapOf("custom:3" to 5),
            ),
        ),
        appearance = AppearanceSnapshot(theme = "gym", accentColor = "blue"),
        preferences = PreferencesSnapshot(
            autoWorkoutCheckin = true,
            notificationEnabled = mapOf("water" to true),
        ),
    )

    @Test
    fun roundTrip_preservaCampos() {
        val original = samplePayload()
        val decoded = BackupCodec.decode(BackupCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun json_usaChavesLegadasDoIos() {
        val json = BackupCodec.encode(samplePayload())
        assertTrue("fitnessGoal", json.contains("\"fitnessGoal\""))
        assertTrue("fats (goals)", json.contains("\"fats\""))
        assertTrue("carb (record)", json.contains("\"carb\""))
        assertTrue("fat (record)", json.contains("\"fat\""))
        assertTrue("catTitle", json.contains("\"catTitle\""))
        assertTrue("catEmoji", json.contains("\"catEmoji\""))
    }

    @Test
    fun encode_omiteOpcionaisNulos() {
        // Sem appearance/preferences, esses campos não devem aparecer no JSON (espelha o nil do iOS).
        val minimal = samplePayload().copy(appearance = null, preferences = null)
        val json = BackupCodec.encode(minimal)
        assertTrue("sem appearance", !json.contains("\"appearance\""))
        assertTrue("sem preferences", !json.contains("\"preferences\""))
        // Mas os obrigatórios continuam, mesmo vazios.
        assertTrue("goalsOrder presente", json.contains("\"goalsOrder\""))
    }

    @Test
    fun decode_ignoraCamposIosOnly() {
        // Um backup do iOS traz campos que o Android não modela; o decode deve ignorá-los sem quebrar.
        val iosJson = """
            {
              "version": 4,
              "exportedAt": "2026-06-08T00:00:00Z",
              "profile": {"name":"A","height":180,"weight":80.0,"age":30,"sex":"male","fitnessGoal":"maintenance"},
              "goals": {"calories":2900,"sleep":7,"water":3250,"protein":180,"carbs":320,"fats":80,"fiber":30},
              "goalsOrder": ["tracking.sleep"],
              "customGoals": [],
              "dailyRecords": [],
              "removedItems": ["x"],
              "customCategories": [{"id":"c1","name":"Foo"}],
              "categoryOrder": ["a"],
              "builtinCategoryOrder": ["Essencial"]
            }
        """.trimIndent()
        val decoded = BackupCodec.decode(iosJson)
        assertEquals("maintenance", decoded.profile.userGoal)
        assertEquals(80, decoded.goals.goodFat)
        assertNull(decoded.preferences)
    }
}
