package com.jonathaxs.gymnutshell.core.domain

/** Categorias dos temas — porte de AppTheme.ThemeCategory (iOS). Ordem = ordem de exibição. */
enum class ThemeCategory { Sport, Animals, Warrior, Space, Elements, Competition }

/**
 * Tema de mascote — porte de AppTheme (iOS). Define o emoji exibido em cada tier de conquista.
 * `rawValue` persiste no DataStore. Os nomes localizados por tier (ex.: "Rooster") virão depois;
 * por ora o tema controla só os emojis. Variantes por sexo também ficam pra depois (usa o base).
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
