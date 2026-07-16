package com.jonathaxs.gymnutshell.core.domain

import androidx.annotation.StringRes
import com.jonathaxs.gymnutshell.core.R

/** Categorias dos temas — porte de AppTheme.ThemeCategory (iOS). Ordem = ordem de exibição. */
enum class ThemeCategory { Sport, Animals, Warrior, Space, Elements, Competition }

/**
 * Tema de mascote — porte de AppTheme (iOS). Define o emoji e o nome exibidos em cada tier de
 * conquista. `rawValue` persiste no DataStore. Variantes por sexo ficam pra depois (usa o base).
 */
enum class AppTheme(val rawValue: String, val category: ThemeCategory) {
    Gym("gym", ThemeCategory.Sport),
    Running("running", ThemeCategory.Sport),
    Cat("cat", ThemeCategory.Animals),
    Dog("dog", ThemeCategory.Animals),
    Bear("bear", ThemeCategory.Animals),
    Dino("dino", ThemeCategory.Animals),
    Dragon("dragon", ThemeCategory.Animals),
    Horse("horse", ThemeCategory.Animals),
    Ocean("ocean", ThemeCategory.Animals),
    Monkey("monkey", ThemeCategory.Animals),
    Bird("bird", ThemeCategory.Animals),
    Doctor("doctor", ThemeCategory.Warrior),
    Ninja("ninja", ThemeCategory.Warrior),
    Fire("fire", ThemeCategory.Elements),
    Plant("plant", ThemeCategory.Elements),
    Astronaut("astronaut", ThemeCategory.Space),
    Celestial("celestial", ThemeCategory.Space),
    Champion("champion", ThemeCategory.Competition),
    Number("number", ThemeCategory.Competition);

    /** Emoji do tema para um tier — valores idênticos ao AppTheme.emoji(for:) do iOS (base). */
    fun emoji(tier: DailyAchievement): String = when (this) {
        Gym -> tier.pick("🐓", "🏋️", "🐀", "💪")
        Running -> tier.pick("🚶", "👟", "🏃", "🏅")
        Cat -> tier.pick("🐱", "🐈", "🐆", "🦁")
        Dog -> tier.pick("🐶", "🐩", "🐕‍🦺", "🐕")
        Bear -> tier.pick("🧸", "🐻", "🐻‍❄️", "🦬")
        Dino -> tier.pick("🐢", "🐊", "🦖", "🦕")
        Dragon -> tier.pick("🥚", "🦎", "🐉", "🐲")
        Horse -> tier.pick("🐴", "🦄", "🏇", "🎠")
        Ocean -> tier.pick("🐟", "🐬", "🪼", "🐋")
        Monkey -> tier.pick("🐒", "🐵", "🦍", "🦧")
        Bird -> tier.pick("🐣", "🐦", "🦅", "🦉")
        Doctor -> tier.pick("🥼", "💉", "⚕️", "👨‍⚕️")
        Ninja -> tier.pick("🥋", "⚔️", "🥷", "🦸")
        Fire -> tier.pick("🕯️", "🔥", "🌋", "🌞")
        Plant -> tier.pick("🌱", "🌿", "🌳", "🎄")
        Astronaut -> tier.pick("🛰️", "🧑‍🚀", "🚀", "🛸")
        Celestial -> tier.pick("☄️", "🌔", "🌎", "🪐")
        Champion -> tier.pick("🥉", "🥈", "🥇", "💎")
        Number -> tier.pick("1️⃣", "2️⃣", "3️⃣", "4️⃣")
    }

    /**
     * Nome do tema para um tier (ex.: Gym + Level1 = "Rooster") — porte de AppTheme.name(for:) do
     * iOS. Devolve o @StringRes; quem resolve é a UI, com `stringResource`/`getString`.
     */
    @StringRes
    fun tierNameRes(tier: DailyAchievement): Int = when (this) {
        Gym -> tier.pick(R.string.tier_gym_level1, R.string.tier_gym_level2, R.string.tier_gym_level3, R.string.tier_gym_level4)
        Running -> tier.pick(R.string.tier_running_level1, R.string.tier_running_level2, R.string.tier_running_level3, R.string.tier_running_level4)
        Cat -> tier.pick(R.string.tier_cat_level1, R.string.tier_cat_level2, R.string.tier_cat_level3, R.string.tier_cat_level4)
        Dog -> tier.pick(R.string.tier_dog_level1, R.string.tier_dog_level2, R.string.tier_dog_level3, R.string.tier_dog_level4)
        Bear -> tier.pick(R.string.tier_bear_level1, R.string.tier_bear_level2, R.string.tier_bear_level3, R.string.tier_bear_level4)
        Dino -> tier.pick(R.string.tier_dino_level1, R.string.tier_dino_level2, R.string.tier_dino_level3, R.string.tier_dino_level4)
        Dragon -> tier.pick(R.string.tier_dragon_level1, R.string.tier_dragon_level2, R.string.tier_dragon_level3, R.string.tier_dragon_level4)
        Horse -> tier.pick(R.string.tier_horse_level1, R.string.tier_horse_level2, R.string.tier_horse_level3, R.string.tier_horse_level4)
        Ocean -> tier.pick(R.string.tier_ocean_level1, R.string.tier_ocean_level2, R.string.tier_ocean_level3, R.string.tier_ocean_level4)
        Monkey -> tier.pick(R.string.tier_monkey_level1, R.string.tier_monkey_level2, R.string.tier_monkey_level3, R.string.tier_monkey_level4)
        Bird -> tier.pick(R.string.tier_bird_level1, R.string.tier_bird_level2, R.string.tier_bird_level3, R.string.tier_bird_level4)
        Doctor -> tier.pick(R.string.tier_doctor_level1, R.string.tier_doctor_level2, R.string.tier_doctor_level3, R.string.tier_doctor_level4)
        Ninja -> tier.pick(R.string.tier_ninja_level1, R.string.tier_ninja_level2, R.string.tier_ninja_level3, R.string.tier_ninja_level4)
        Fire -> tier.pick(R.string.tier_fire_level1, R.string.tier_fire_level2, R.string.tier_fire_level3, R.string.tier_fire_level4)
        Plant -> tier.pick(R.string.tier_plant_level1, R.string.tier_plant_level2, R.string.tier_plant_level3, R.string.tier_plant_level4)
        Astronaut -> tier.pick(R.string.tier_astronaut_level1, R.string.tier_astronaut_level2, R.string.tier_astronaut_level3, R.string.tier_astronaut_level4)
        Celestial -> tier.pick(R.string.tier_celestial_level1, R.string.tier_celestial_level2, R.string.tier_celestial_level3, R.string.tier_celestial_level4)
        Champion -> tier.pick(R.string.tier_champion_level1, R.string.tier_champion_level2, R.string.tier_champion_level3, R.string.tier_champion_level4)
        Number -> tier.pick(R.string.tier_number_level1, R.string.tier_number_level2, R.string.tier_number_level3, R.string.tier_number_level4)
    }

    /** Os 4 emojis do tema, do tier 1 ao 4 — usado em prévias do seletor. */
    val previewEmojis: List<String>
        get() = DailyAchievement.entries.map { emoji(it) }

    companion object {
        val Default = Gym
        fun fromRaw(raw: String?): AppTheme = entries.firstOrNull { it.rawValue == raw } ?: Default
        fun inCategory(category: ThemeCategory): List<AppTheme> = entries.filter { it.category == category }
    }
}

/** Atalho: escolhe um dos 4 valores conforme o tier. */
private fun <T> DailyAchievement.pick(l1: T, l2: T, l3: T, l4: T): T = when (this) {
    DailyAchievement.Level1 -> l1
    DailyAchievement.Level2 -> l2
    DailyAchievement.Level3 -> l3
    DailyAchievement.Level4 -> l4
}
