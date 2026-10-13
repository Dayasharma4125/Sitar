package com.example.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.model.AppSettings
import com.example.model.Track
import java.util.Locale

enum class MusicFontVibe(
    val title: String,
    val description: String,
    val fontFamily: FontFamily,
    val previewText: String
) {
    DEFAULT_SANS(
        title = "Modern Sans",
        description = "Clean, highly legible Material typography",
        fontFamily = FontFamily.Default,
        previewText = "Clean & Crisp Sans"
    ),
    ELEGANT_SERIF(
        title = "Editorial Serif",
        description = "Acoustic, classical, indie & orchestral aesthetic",
        fontFamily = FontFamily.Serif,
        previewText = "Orchestral & Classical"
    ),
    CYBER_MONOSPACE(
        title = "Cyber Monospace",
        description = "Electronic, synthwave, techno & 8-bit aesthetic",
        fontFamily = FontFamily.Monospace,
        previewText = "Synthesizer & Cyber"
    ),
    CURSIVE_SOUL(
        title = "Soulful Cursive",
        description = "Jazz, blues, vintage soul & expressive rhythm",
        fontFamily = FontFamily.Cursive,
        previewText = "Soul & Jazz Rhythm"
    ),
    SANS_SERIF(
        title = "Grotesque Sans",
        description = "Bold contemporary pop & dance typography",
        fontFamily = FontFamily.SansSerif,
        previewText = "Pop & High Energy"
    )
}

object MusicFontResolver {

    /**
     * Resolves the appropriate FontFamily according to track metadata and user settings.
     */
    fun resolveFontFamily(track: Track?, settings: AppSettings): FontFamily {
        if (!settings.changeFontsWithMusic) {
            return getFontFamilyByName(settings.defaultFontFamily)
        }

        if (track == null || track.id.isBlank()) {
            return FontFamily.Default
        }

        val text = "${track.title} ${track.artist} ${track.album}".lowercase(Locale.ROOT)

        return when {
            // Classical / Acoustic / Piano / Orchestral / Folk / Indie
            text.contains("classical") || text.contains("piano") || text.contains("acoustic") ||
                text.contains("orchestra") || text.contains("symphony") || text.contains("violin") ||
                text.contains("guitar") || text.contains("folk") || text.contains("indie") ||
                text.contains("unplugged") || text.contains("ballad") -> FontFamily.Serif

            // Techno / Synth / 8-bit / Electronic / Cyber / Metal / Rock
            text.contains("techno") || text.contains("synth") || text.contains("cyber") ||
                text.contains("electro") || text.contains("8bit") || text.contains("8-bit") ||
                text.contains("remix") || text.contains("metal") || text.contains("rock") ||
                text.contains("bass") || text.contains("dubstep") -> FontFamily.Monospace

            // Jazz / Soul / Blues / R&B / Romance / Vintage
            text.contains("jazz") || text.contains("soul") || text.contains("blues") ||
                text.contains("r&b") || text.contains("rnb") || text.contains("sax") ||
                text.contains("love") || text.contains("romance") || text.contains("vintage") -> FontFamily.Cursive

            // Pop / Dance / Hip-Hop / Rap
            text.contains("pop") || text.contains("dance") || text.contains("hip hop") ||
                text.contains("rap") || text.contains("party") || text.contains("club") ||
                text.contains("trap") -> FontFamily.SansSerif

            else -> {
                // Deterministic vibe based on track title hash
                val hash = kotlin.math.abs(track.title.hashCode() + track.artist.hashCode()) % 4
                when (hash) {
                    0 -> FontFamily.Default
                    1 -> FontFamily.Serif
                    2 -> FontFamily.SansSerif
                    else -> FontFamily.Monospace
                }
            }
        }
    }

    fun getFontFamilyByName(name: String): FontFamily {
        return when (name.uppercase(Locale.ROOT)) {
            "SERIF" -> FontFamily.Serif
            "MONOSPACE" -> FontFamily.Monospace
            "CURSIVE" -> FontFamily.Cursive
            "SANS_SERIF" -> FontFamily.SansSerif
            else -> FontFamily.Default
        }
    }

    fun getVibeDescription(track: Track?, settings: AppSettings): String {
        if (!settings.changeFontsWithMusic) {
            return "Static: ${settings.defaultFontFamily.lowercase().replaceFirstChar { it.uppercase() }}"
        }
        val family = resolveFontFamily(track, settings)
        return when (family) {
            FontFamily.Serif -> "Dynamic: Editorial Serif (Acoustic / Classical)"
            FontFamily.Monospace -> "Dynamic: Cyber Monospace (Electronic / Synth)"
            FontFamily.Cursive -> "Dynamic: Soulful Cursive (Jazz / R&B)"
            FontFamily.SansSerif -> "Dynamic: Grotesque Sans (Pop / Energetic)"
            else -> "Dynamic: Modern Sans (Balanced)"
        }
    }
}
