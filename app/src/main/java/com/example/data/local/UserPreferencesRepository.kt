package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hisobim_prefs")

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val password: String = "",
    val currencySymbol: String = "so'm",
    val currencyDisplay: String = "So'm (UZS)",
    val monthlyCharityGoal: Long = 300_000L,
    val lastBackupLabel: String = "Hali olinmagan"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PASSWORD = stringPreferencesKey("app_password")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CURRENCY_DISPLAY = stringPreferencesKey("currency_display")
        val MONTHLY_CHARITY_GOAL = longPreferencesKey("monthly_charity_goal")
        val LAST_BACKUP_LABEL = stringPreferencesKey("last_backup_label")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val modeName = prefs[Keys.THEME_MODE] ?: AppThemeMode.LIGHT.name
        UserPreferences(
            themeMode = AppThemeMode.entries.firstOrNull { it.name == modeName } ?: AppThemeMode.LIGHT,
            password = prefs[Keys.PASSWORD] ?: "",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "so'm",
            currencyDisplay = prefs[Keys.CURRENCY_DISPLAY] ?: "So'm (UZS)",
            monthlyCharityGoal = prefs[Keys.MONTHLY_CHARITY_GOAL] ?: 300_000L,
            lastBackupLabel = prefs[Keys.LAST_BACKUP_LABEL] ?: "Hali olinmagan"
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { it[Keys.PASSWORD] = password }
    }

    suspend fun setCurrency(symbol: String, display: String) {
        context.dataStore.edit {
            it[Keys.CURRENCY_SYMBOL] = symbol
            it[Keys.CURRENCY_DISPLAY] = display
        }
    }

    suspend fun setMonthlyCharityGoal(goal: Long) {
        context.dataStore.edit { it[Keys.MONTHLY_CHARITY_GOAL] = goal.coerceAtLeast(0L) }
    }

    suspend fun setLastBackupLabel(label: String) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_LABEL] = label }
    }
}
