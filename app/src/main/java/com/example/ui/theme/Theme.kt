package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    LIGHT("Kun"),
    DARK("Tun"),
    AUTO("Avto")
}

@Immutable
data class HisobimColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val segmentTrack: Color,
    val divider: Color,
    val border: Color,
    val primaryAccent: Color,          // Selected pills, active tab, links
    val onPrimaryAccent: Color,
    val primaryButton: Color,          // Saqlash, + Kirim, + Qarz/Reja/Hayriya
    val onPrimaryButton: Color,
    val incomeText: Color,
    val expenseColor: Color,
    val onExpenseColor: Color,
    val iconBadgeBg: Color,
    val iconBadgeTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val loginBg: Color,
    val loginIconBox: Color,
    val loginIconTint: Color,
    val loginButtonBg: Color,
    val loginButtonText: Color,
    val loginLinkText: Color,
    val chartPalette: List<Color>
)

private val LightHisobimColors = HisobimColors(
    isDark = false,
    background = LightBg,
    card = LightSurface,
    segmentTrack = LightSurfaceMuted,
    divider = LightDivider,
    border = LightBorder,
    primaryAccent = LightEmerald,
    onPrimaryAccent = Color.White,
    primaryButton = LightEmerald,
    onPrimaryButton = Color.White,
    incomeText = LightEmerald,
    expenseColor = LightTerracotta,
    onExpenseColor = Color.White,
    iconBadgeBg = LightEmeraldSoft,
    iconBadgeTint = LightEmerald,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    loginBg = LightEmerald,
    loginIconBox = Color.White,
    loginIconTint = LightEmerald,
    loginButtonBg = Color.White,
    loginButtonText = LightEmerald,
    loginLinkText = Color.White,
    chartPalette = LightChartColors
)

private val DarkHisobimColors = HisobimColors(
    isDark = true,
    background = DarkBg,
    card = DarkSurface,
    segmentTrack = DarkSurface,
    divider = DarkDivider,
    border = DarkBorder,
    primaryAccent = DarkLavender,
    onPrimaryAccent = DarkOnLavender,
    primaryButton = DarkMint,
    onPrimaryButton = DarkOnMint,
    incomeText = DarkMint,
    expenseColor = DarkCoral,
    onExpenseColor = DarkOnCoral,
    iconBadgeBg = DarkSurfaceElevated,
    iconBadgeTint = DarkLavender,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    loginBg = DarkBg,
    loginIconBox = DarkLavender,
    loginIconTint = DarkOnLavender,
    loginButtonBg = DarkMint,
    loginButtonText = DarkOnMint,
    loginLinkText = DarkLavender,
    chartPalette = DarkChartColors
)

val LocalHisobimColors = staticCompositionLocalOf { LightHisobimColors }

private val HisobimLightColorScheme = lightColorScheme(
    primary = LightEmerald,
    onPrimary = Color.White,
    primaryContainer = LightEmeraldSoft,
    onPrimaryContainer = LightEmerald,
    secondary = LightTerracotta,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceMuted,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider,
    error = LightTerracotta,
    onError = Color.White
)

private val HisobimDarkColorScheme = darkColorScheme(
    primary = DarkLavender,
    onPrimary = DarkOnLavender,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkLavender,
    secondary = DarkMint,
    onSecondary = DarkOnMint,
    tertiary = DarkCoral,
    onTertiary = DarkOnCoral,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkDivider,
    error = DarkCoral,
    onError = DarkOnCoral
)

@Composable
fun HisobimTheme(
    themeMode: AppThemeMode = AppThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) HisobimDarkColorScheme else HisobimLightColorScheme
    val hisobimColors = if (isDark) DarkHisobimColors else LightHisobimColors

    CompositionLocalProvider(LocalHisobimColors provides hisobimColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
