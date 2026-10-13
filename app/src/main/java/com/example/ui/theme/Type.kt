package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Track

// Define installed static Google Fonts
val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold),
    Font(R.font.outfit, FontWeight.ExtraBold)
)

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.Bold)
)

enum class MusicTypographyStyle {
    MODERN_SANS,    // Outfit
    SPACE_CYBER,    // Space Grotesk
    ELEGANT_SERIF,  // Playfair Display
    SYSTEM_DEFAULT
}

/**
 * Dynamically resolves the best expressive font family for each track based on metadata and style.
 */
fun resolveMusicTypographyStyle(track: Track): MusicTypographyStyle {
    val text = (track.title + " " + track.artist + " " + track.album).lowercase()
    return when {
        text.contains("classical") || text.contains("orchestra") || text.contains("acoustic") ||
        text.contains("piano") || text.contains("jazz") || text.contains("soul") ||
        text.contains("vintage") || text.contains("symphony") || text.contains("ballad") ||
        text.contains("noir") || text.contains("love") || text.contains("acoustic") -> MusicTypographyStyle.ELEGANT_SERIF

        text.contains("cyber") || text.contains("electro") || text.contains("synth") ||
        text.contains("techno") || text.contains("future") || text.contains("remix") ||
        text.contains("bass") || text.contains("edm") || text.contains("dance") ||
        text.contains("house") || text.contains("wave") || text.contains("hip hop") ||
        text.contains("rap") || text.contains("trap") -> MusicTypographyStyle.SPACE_CYBER

        else -> MusicTypographyStyle.MODERN_SANS
    }
}

fun getFontFamilyForMusic(
    track: Track,
    changeFontsWithMusic: Boolean = true,
    defaultFontFamily: String = "DEFAULT"
): FontFamily {
    if (!changeFontsWithMusic) {
        return when (defaultFontFamily.uppercase(java.util.Locale.ROOT)) {
            "SERIF", "PLAYFAIR" -> PlayfairDisplayFontFamily
            "MONOSPACE", "GROTESK", "SPACE_GROTESK" -> SpaceGroteskFontFamily
            "SANS", "OUTFIT" -> OutfitFontFamily
            else -> OutfitFontFamily
        }
    }
    return when (resolveMusicTypographyStyle(track)) {
        MusicTypographyStyle.ELEGANT_SERIF -> PlayfairDisplayFontFamily
        MusicTypographyStyle.SPACE_CYBER -> SpaceGroteskFontFamily
        MusicTypographyStyle.MODERN_SANS -> OutfitFontFamily
        MusicTypographyStyle.SYSTEM_DEFAULT -> OutfitFontFamily
    }
}

/**
 * Returns dynamic Material 3 Typography tuned with the active music font family.
 */
fun createDynamicMusicTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 54.sp,
            lineHeight = 62.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            lineHeight = 50.sp,
            letterSpacing = (-0.5).sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 42.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.2).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.6.sp
        )
    )
}

// Default Global App Typography
val Typography = createDynamicMusicTypography(OutfitFontFamily)
