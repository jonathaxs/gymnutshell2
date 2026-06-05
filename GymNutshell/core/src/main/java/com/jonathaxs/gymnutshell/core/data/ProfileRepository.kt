package com.jonathaxs.gymnutshell.core.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathaxs.gymnutshell.core.domain.Profile
import com.jonathaxs.gymnutshell.core.domain.UserGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persiste o perfil do usuário no DataStore — porte das chaves UserProfile (UserDefaults no iOS).
 * As chaves de preferência reusam exatamente as mesmas strings do iOS ("profile.weight", etc.).
 */
class ProfileRepository(private val context: Context) {

    private object Keys {
        val name = stringPreferencesKey("profile.name")
        val weight = doublePreferencesKey("profile.weight")
        val height = intPreferencesKey("profile.height")
        val age = intPreferencesKey("profile.age")
        val sex = stringPreferencesKey("profile.sex")
        val goal = stringPreferencesKey("profile.userGoal")
    }

    val profile: Flow<Profile> = context.appPreferences.data.map { prefs ->
        Profile(
            name = prefs[Keys.name] ?: "",
            weightKg = prefs[Keys.weight] ?: 0.0,
            heightCm = prefs[Keys.height] ?: 0,
            age = prefs[Keys.age] ?: 0,
            sex = prefs[Keys.sex] ?: "other",
            goal = UserGoal.fromRaw(prefs[Keys.goal]) ?: UserGoal.Maintenance,
        )
    }

    suspend fun update(profile: Profile) {
        context.appPreferences.edit { prefs ->
            prefs[Keys.name] = profile.name
            prefs[Keys.weight] = profile.weightKg
            prefs[Keys.height] = profile.heightCm
            prefs[Keys.age] = profile.age
            prefs[Keys.sex] = profile.sex
            prefs[Keys.goal] = profile.goal.rawValue
        }
    }
}
