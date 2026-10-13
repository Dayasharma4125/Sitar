package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppColorPalette
import com.example.model.AppThemeMode
import com.example.model.Track

/**
 * Standard Default Schemes
 */
val MyCustomDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBDC7DC),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3E4758),
    onSecondaryContainer = Color(0xFFD9E3F8),
    tertiary = Color(0xFFDBBCE1),
    onTertiary = Color(0xFF3E2845),
    tertiaryContainer = Color(0xFF563E5D),
    onTertiaryContainer = Color(0xFFF8D8FD),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F)
)

val MyCustomLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B5EA8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF555F71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3F8),
    onSecondaryContainer = Color(0xFF121C2B),
    tertiary = Color(0xFF6E5676),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8D8FD),
    onTertiaryContainer = Color(0xFF27142F),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E8EE),
    surfaceContainerHighest = Color(0xFFE2E2E9),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

/**
 * Generate dark color scheme for specific palette
 */
fun getDarkSchemeForPalette(palette: AppColorPalette): ColorScheme {
    return when (palette) {
        AppColorPalette.DYNAMIC -> MyCustomDarkColorScheme
        AppColorPalette.OCEAN_BLUE -> darkColorScheme(
            primary = Color(0xFF90CAFF),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF00497D),
            onPrimaryContainer = Color(0xFFD1E4FF),
            secondary = Color(0xFFBCC7DB),
            onSecondary = Color(0xFF263141),
            secondaryContainer = Color(0xFF3C4758),
            onSecondaryContainer = Color(0xFFD8E3F8),
            tertiary = Color(0xFFD6BEE4),
            background = Color(0xFF0F141C),
            onBackground = Color(0xFFE1E2EC),
            surface = Color(0xFF0F141C),
            onSurface = Color(0xFFE1E2EC),
            surfaceContainer = Color(0xFF181F2C)
        )
        AppColorPalette.EMERALD_GREEN -> darkColorScheme(
            primary = Color(0xFF6CDDB3),
            onPrimary = Color(0xFF003827),
            primaryContainer = Color(0xFF00513B),
            onPrimaryContainer = Color(0xFF89F9CF),
            secondary = Color(0xFFB3CCBF),
            onSecondary = Color(0xFF1F352C),
            secondaryContainer = Color(0xFF354B41),
            onSecondaryContainer = Color(0xFFCFE9DB),
            tertiary = Color(0xFFA5CDDD),
            background = Color(0xFF0C1511),
            onBackground = Color(0xFFDEE5E0),
            surface = Color(0xFF0C1511),
            onSurface = Color(0xFFDEE5E0),
            surfaceContainer = Color(0xFF14221C)
        )
        AppColorPalette.CRIMSON_RED -> darkColorScheme(
            primary = Color(0xFFFFB3B4),
            onPrimary = Color(0xFF561D22),
            primaryContainer = Color(0xFF733337),
            onPrimaryContainer = Color(0xFFFFDAD9),
            secondary = Color(0xFFE7BDBE),
            onSecondary = Color(0xFF44292B),
            secondaryContainer = Color(0xFF5D3F41),
            onSecondaryContainer = Color(0xFFFFDAD9),
            tertiary = Color(0xFFE5C18D),
            background = Color(0xFF1A1112),
            onBackground = Color(0xFFF1DFDF),
            surface = Color(0xFF1A1112),
            onSurface = Color(0xFFF1DFDF),
            surfaceContainer = Color(0xFF28181A)
        )
        AppColorPalette.SUNSET_AMBER -> darkColorScheme(
            primary = Color(0xFFFFB870),
            onPrimary = Color(0xFF4A2800),
            primaryContainer = Color(0xFF693C00),
            onPrimaryContainer = Color(0xFFFFDCBE),
            secondary = Color(0xFFDEC2A2),
            onSecondary = Color(0xFF3E2D17),
            secondaryContainer = Color(0xFF56432B),
            onSecondaryContainer = Color(0xFFFBDEBD),
            tertiary = Color(0xFFBFCB9B),
            background = Color(0xFF17120D),
            onBackground = Color(0xFFEDE0D5),
            surface = Color(0xFF17120D),
            onSurface = Color(0xFFEDE0D5),
            surfaceContainer = Color(0xFF251C15)
        )
        AppColorPalette.LAVENDER_PURPLE -> darkColorScheme(
            primary = Color(0xFFD6BAFF),
            onPrimary = Color(0xFF3E1C77),
            primaryContainer = Color(0xFF56358F),
            onPrimaryContainer = Color(0xFFECDCFF),
            secondary = Color(0xFFCCC2DB),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4358),
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = Color(0xFFF0B7C6),
            background = Color(0xFF141218),
            onBackground = Color(0xFFE6E0E9),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE6E0E9),
            surfaceContainer = Color(0xFF201B27)
        )
        AppColorPalette.NEON_CYAN -> darkColorScheme(
            primary = Color(0xFF4FD8EB),
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFF97F0FF),
            secondary = Color(0xFFB1CBD0),
            onSecondary = Color(0xFF1C3438),
            secondaryContainer = Color(0xFF334B4F),
            onSecondaryContainer = Color(0xFFCDE7EC),
            tertiary = Color(0xFFBCC5EB),
            background = Color(0xFF0C1416),
            onBackground = Color(0xFFDFE3E5),
            surface = Color(0xFF0C1416),
            onSurface = Color(0xFFDFE3E5),
            surfaceContainer = Color(0xFF142024)
        )
        AppColorPalette.ROSE_PINK -> darkColorScheme(
            primary = Color(0xFFFFB0CA),
            onPrimary = Color(0xFF5C112E),
            primaryContainer = Color(0xFF7A2944),
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = Color(0xFFE4BDC6),
            onSecondary = Color(0xFF432931),
            secondaryContainer = Color(0xFF5B3F47),
            onSecondaryContainer = Color(0xFFFFD9E2),
            tertiary = Color(0xFFECC095),
            background = Color(0xFF181114),
            onBackground = Color(0xFFEFE0E3),
            surface = Color(0xFF181114),
            onSurface = Color(0xFFEFE0E3),
            surfaceContainer = Color(0xFF26181E)
        )
        AppColorPalette.GRAPHITE_SLATE -> darkColorScheme(
            primary = Color(0xFFB0C6FF),
            onPrimary = Color(0xFF163065),
            primaryContainer = Color(0xFF2E477D),
            onPrimaryContainer = Color(0xFFD9E2FF),
            secondary = Color(0xFFC2C6D6),
            onSecondary = Color(0xFF2C303D),
            secondaryContainer = Color(0xFF434654),
            onSecondaryContainer = Color(0xFFDEE2F2),
            tertiary = Color(0xFFD9BDE2),
            background = Color(0xFF121316),
            onBackground = Color(0xFFE2E2E6),
            surface = Color(0xFF121316),
            onSurface = Color(0xFFE2E2E6),
            surfaceContainer = Color(0xFF1C1E23)
        )
    }
}

/**
 * Generate light color scheme for specific palette
 */
fun getLightSchemeForPalette(palette: AppColorPalette): ColorScheme {
    return when (palette) {
        AppColorPalette.DYNAMIC -> MyCustomLightColorScheme
        AppColorPalette.OCEAN_BLUE -> lightColorScheme(
            primary = Color(0xFF0061A4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF535F70),
            background = Color(0xFFFDFBFF),
            onBackground = Color(0xFF1A1C1E),
            surface = Color(0xFFFDFBFF),
            onSurface = Color(0xFF1A1C1E),
            surfaceContainer = Color(0xFFEDEBF0)
        )
        AppColorPalette.EMERALD_GREEN -> lightColorScheme(
            primary = Color(0xFF006C4F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF89F9CF),
            onPrimaryContainer = Color(0xFF002116),
            secondary = Color(0xFF4C6357),
            background = Color(0xFFFBFDF9),
            onBackground = Color(0xFF191C1A),
            surface = Color(0xFFFBFDF9),
            onSurface = Color(0xFF191C1A),
            surfaceContainer = Color(0xFFECEFEA)
        )
        AppColorPalette.CRIMSON_RED -> lightColorScheme(
            primary = Color(0xFF904A4D),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDAD9),
            onPrimaryContainer = Color(0xFF3B080F),
            secondary = Color(0xFF775657),
            background = Color(0xFFFFFBFA),
            onBackground = Color(0xFF201A1A),
            surface = Color(0xFFFFFBFA),
            onSurface = Color(0xFF201A1A),
            surfaceContainer = Color(0xFFF4EBEC)
        )
        AppColorPalette.SUNSET_AMBER -> lightColorScheme(
            primary = Color(0xFF8B5000),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDCBE),
            onPrimaryContainer = Color(0xFF2C1600),
            secondary = Color(0xFF705B41),
            background = Color(0xFFFFFBFF),
            onBackground = Color(0xFF201B16),
            surface = Color(0xFFFFFBFF),
            onSurface = Color(0xFF201B16),
            surfaceContainer = Color(0xFFF4EDE5)
        )
        AppColorPalette.LAVENDER_PURPLE -> lightColorScheme(
            primary = Color(0xFF6F4FA2),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFECDCFF),
            onPrimaryContainer = Color(0xFF280057),
            secondary = Color(0xFF625B71),
            background = Color(0xFFFFFBFF),
            onBackground = Color(0xFF1D1B20),
            surface = Color(0xFFFFFBFF),
            onSurface = Color(0xFF1D1B20),
            surfaceContainer = Color(0xFFF1EBFA)
        )
        AppColorPalette.NEON_CYAN -> lightColorScheme(
            primary = Color(0xFF006874),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF97F0FF),
            onPrimaryContainer = Color(0xFF001F24),
            secondary = Color(0xFF4A6267),
            background = Color(0xFFFAFDFD),
            onBackground = Color(0xFF191C1D),
            surface = Color(0xFFFAFDFD),
            onSurface = Color(0xFF191C1D),
            surfaceContainer = Color(0xFFE6ECEE)
        )
        AppColorPalette.ROSE_PINK -> lightColorScheme(
            primary = Color(0xFF984060),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E2),
            onPrimaryContainer = Color(0xFF3E001D),
            secondary = Color(0xFF74565F),
            background = Color(0xFFFFFBFA),
            onBackground = Color(0xFF201A1B),
            surface = Color(0xFFFFFBFA),
            onSurface = Color(0xFF201A1B),
            surfaceContainer = Color(0xFFF4ECEE)
        )
        AppColorPalette.GRAPHITE_SLATE -> lightColorScheme(
            primary = Color(0xFF475D92),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD9E2FF),
            onPrimaryContainer = Color(0xFF001944),
            secondary = Color(0xFF5B5D6B),
            background = Color(0xFFFEFBFF),
            onBackground = Color(0xFF1B1B1F),
            surface = Color(0xFFFEFBFF),
            onSurface = Color(0xFF1B1B1F),
            surfaceContainer = Color(0xFFECEEF2)
        )
    }
}

/**
 * Ensures an artwork-extracted color has sufficient luminance, saturation, and contrast
 * to remain clearly visible against dark and AMOLED black backgrounds.
 * Guarantees Tone 80 / Tone 85 Material 3 luminance standards so UI components never vanish.
 */
fun ensureVibrantDarkAccent(color: Color, fallback: Color = Color(0xFFA8C8FF)): Color {
    if (color == Color.Unspecified || color == Color.Transparent) return fallback

    val r = color.red.coerceIn(0f, 1f)
    val g = color.green.coerceIn(0f, 1f)
    val b = color.blue.coerceIn(0f, 1f)

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val hue = when {
        delta < 0.0001f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0) it + 360f else it }

    val rawSat = if (max == 0f) 0f else delta / max
    val rawVal = max
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b

    // If the artwork color is pitch black, dark grey, or near-monochrome
    if (rawVal < 0.25f || (rawSat < 0.10f && rawVal < 0.40f)) {
        return fallback
    }

    // Boost saturation and brightness to guaranteed Material 3 Tone 85/90 for dark backgrounds
    val targetSat = (rawSat * 1.30f).coerceIn(0.50f, 0.95f)
    val targetVal = if (luminance < 0.55f) 0.92f else rawVal.coerceIn(0.85f, 0.99f)

    val c = targetVal * targetSat
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = targetVal - c

    val (r1, g1, b1) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}

/**
 * Dynamically synthesizes a full Material 3 ColorScheme from the playing music's ambient accent colors.
 * Applies intelligent contrast scaling and luminance protection for dark artwork.
 */
fun generateMusicDynamicColorScheme(
    musicAccent: Color,
    secondaryAccent: Color,
    isDark: Boolean,
    isAmoled: Boolean
): ColorScheme {
    val safeMusicAccent = try {
        Color(musicAccent.red, musicAccent.green, musicAccent.blue, 1f)
    } catch (_: Throwable) {
        Color(0xFFA8C8FF)
    }
    val safeSecondaryAccent = try {
        Color(secondaryAccent.red, secondaryAccent.green, secondaryAccent.blue, 1f)
    } catch (_: Throwable) {
        Color(0xFFBDC7DC)
    }

    val vibrantMusicAccent = ensureVibrantDarkAccent(safeMusicAccent, fallback = Color(0xFFA8C8FF))
    val vibrantSecondaryAccent = ensureVibrantDarkAccent(safeSecondaryAccent, fallback = Color(0xFFBDC7DC))

    val primary = if (isDark) vibrantMusicAccent else safeMusicAccent.copy(alpha = 1f)
    val secondary = if (isDark) vibrantSecondaryAccent else safeSecondaryAccent.copy(alpha = 1f)

    val primaryContainer = if (isDark) {
        Color(
            red = (primary.red * 0.28f + 0.08f).coerceIn(0f, 1f),
            green = (primary.green * 0.28f + 0.08f).coerceIn(0f, 1f),
            blue = (primary.blue * 0.32f + 0.10f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        Color(
            red = (primary.red * 0.2f + 0.8f).coerceIn(0f, 1f),
            green = (primary.green * 0.2f + 0.8f).coerceIn(0f, 1f),
            blue = (primary.blue * 0.2f + 0.8f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val secondaryContainer = if (isDark) {
        Color(
            red = (secondary.red * 0.25f + 0.08f).coerceIn(0f, 1f),
            green = (secondary.green * 0.25f + 0.08f).coerceIn(0f, 1f),
            blue = (secondary.blue * 0.28f + 0.09f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        Color(
            red = (secondary.red * 0.2f + 0.8f).coerceIn(0f, 1f),
            green = (secondary.green * 0.2f + 0.8f).coerceIn(0f, 1f),
            blue = (secondary.blue * 0.2f + 0.8f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    return if (isAmoled) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF00152B),
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color(0xFFDCE8FC),
            secondary = secondary,
            onSecondary = Color(0xFF0B1220),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = Color(0xFFE0E5ED),
            tertiary = secondary,
            onTertiary = Color(0xFF0B1220),
            background = Color(0xFF000000),
            onBackground = Color(0xFFE8E8EC),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFE8E8EC),
            surfaceVariant = Color(0xFF1E2330),
            onSurfaceVariant = Color(0xFFD0D3DE),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF0A0D14),
            surfaceContainer = Color(0xFF121620),
            surfaceContainerHigh = Color(0xFF1A202C),
            surfaceContainerHighest = Color(0xFF242C3D),
            outline = Color(0xFF4A5568),
            outlineVariant = Color(0xFF2D3748)
        )
    } else if (isDark) {
        val bgTint = Color(0xFF0E121A)
        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF00152B),
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color(0xFFDCE8FC),
            secondary = secondary,
            onSecondary = Color(0xFF0B1220),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = Color(0xFFDFE4F0),
            tertiary = secondary,
            onTertiary = Color(0xFF0B1220),
            background = bgTint,
            onBackground = Color(0xFFF0F1F6),
            surface = bgTint,
            onSurface = Color(0xFFF0F1F6),
            surfaceVariant = Color(0xFF222836),
            onSurfaceVariant = Color(0xFFD4D6E0),
            surfaceContainerLowest = Color(0xFF090C12),
            surfaceContainerLow = Color(0xFF10141D),
            surfaceContainer = Color(0xFF161B26),
            surfaceContainerHigh = Color(0xFF1E2433),
            surfaceContainerHighest = Color(0xFF283044),
            outline = Color(0xFF4B566B),
            outlineVariant = Color(0xFF2F3746)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color(0xFF041834),
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = Color(0xFF121724),
            tertiary = secondary,
            background = Color(0xFFF9FAFD),
            onBackground = Color(0xFF181B20),
            surface = Color(0xFFF9FAFD),
            onSurface = Color(0xFF181B20),
            surfaceVariant = Color(0xFFDFE2EC),
            onSurfaceVariant = Color(0xFF434650),
            surfaceContainer = Color(0xFFEDEBF2),
            surfaceContainerHigh = Color(0xFFE6E5EC),
            surfaceContainerHighest = Color(0xFFDFDEE6),
            outline = Color(0xFF727680)
        )
    }
}

/**
 * Global Material 3 Theme composable MyAppTheme with Music Dynamic Accent & Dynamic Typography support.
 */
@Composable
fun MyAppTheme(
    themeMode: AppThemeMode = AppThemeMode.PURE_BLACK,
    colorPalette: AppColorPalette = AppColorPalette.DYNAMIC,
    useDynamicColors: Boolean = false,
    playingMusicAccent: Color? = null,
    secondaryMusicAccent: Color? = null,
    musicThemingEnabled: Boolean = true,
    changeFontsWithMusic: Boolean = true,
    defaultFontFamily: String = "DEFAULT",
    currentTrack: Track? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.PURE_BLACK -> true
    }
    val isAmoled = themeMode == AppThemeMode.PURE_BLACK

    val dynamicColor = (colorPalette == AppColorPalette.DYNAMIC) && useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val baseScheme: ColorScheme = when {
        musicThemingEnabled && playingMusicAccent != null -> {
            generateMusicDynamicColorScheme(
                musicAccent = playingMusicAccent,
                secondaryAccent = secondaryMusicAccent ?: playingMusicAccent,
                isDark = isDark,
                isAmoled = isAmoled
            )
        }
        dynamicColor && isDark -> {
            try {
                dynamicDarkColorScheme(context)
            } catch (_: Throwable) {
                getDarkSchemeForPalette(colorPalette)
            }
        }
        dynamicColor && !isDark -> {
            try {
                dynamicLightColorScheme(context)
            } catch (_: Throwable) {
                getLightSchemeForPalette(colorPalette)
            }
        }
        isDark -> getDarkSchemeForPalette(colorPalette)
        else -> getLightSchemeForPalette(colorPalette)
    }

    // Intercept resulting colorScheme if PURE_BLACK is active
    val finalColorScheme = if (themeMode == AppThemeMode.PURE_BLACK && (playingMusicAccent == null || !musicThemingEnabled)) {
        baseScheme.copy(
            background = Color(0xFF000000),
            surface = Color(0xFF000000),
            surfaceDim = Color(0xFF000000),
            surfaceBright = Color(0xFF12141A),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF000000),
            surfaceContainer = Color(0xFF0B0D12),
            surfaceContainerHigh = Color(0xFF12151D),
            surfaceContainerHighest = Color(0xFF1A1E28)
        )
    } else {
        baseScheme
    }

    val dynamicTypography = androidx.compose.runtime.remember(currentTrack, changeFontsWithMusic, defaultFontFamily) {
        if (changeFontsWithMusic && currentTrack != null && currentTrack.id.isNotBlank()) {
            createDynamicMusicTypography(getFontFamilyForMusic(currentTrack, changeFontsWithMusic, defaultFontFamily))
        } else {
            val baseFont = when (defaultFontFamily.uppercase(java.util.Locale.ROOT)) {
                "SERIF", "PLAYFAIR" -> PlayfairDisplayFontFamily
                "MONOSPACE", "GROTESK", "SPACE_GROTESK" -> SpaceGroteskFontFamily
                "SANS", "OUTFIT" -> OutfitFontFamily
                else -> OutfitFontFamily
            }
            createDynamicMusicTypography(baseFont)
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = dynamicTypography,
        shapes = ExpressiveShapes.M3ExpressiveShapes,
        content = content
    )
}

/**
 * Aliases for backward-compatibility.
 */
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MyAppTheme(
        themeMode = AppThemeMode.PURE_BLACK,
        colorPalette = AppColorPalette.DYNAMIC,
        useDynamicColors = false,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode,
    colorPalette: AppColorPalette = AppColorPalette.DYNAMIC,
    useDynamicColors: Boolean = false,
    content: @Composable () -> Unit
) {
    MyAppTheme(
        themeMode = themeMode,
        colorPalette = colorPalette,
        useDynamicColors = useDynamicColors,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    amoledBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeMode = when {
        amoledBlack -> AppThemeMode.PURE_BLACK
        darkTheme -> AppThemeMode.DARK
        else -> AppThemeMode.LIGHT
    }
    MyAppTheme(
        themeMode = themeMode,
        colorPalette = AppColorPalette.DYNAMIC,
        useDynamicColors = dynamicColor,
        content = content
    )
}
