package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A single timestamped line of lyrics.
 */
@Immutable
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

/**
 * Lyrics loading state according to the required 3-step sequence:
 * 1. Embedded tags -> 2. LRCLIB API -> 3. NotFound
 */
sealed interface LyricsUiState {
    data object Loading : LyricsUiState
    data class Success(
        val track: Track,
        val lines: List<LyricLine>,
        val currentPlaybackPositionMs: Long = 0L,
        val isPlaying: Boolean = false,
        val isAutoScrollEnabled: Boolean = true,
        val isFavorite: Boolean = false,
        val isSynced: Boolean = true,
        val plainLyrics: String? = null
    ) : LyricsUiState
    data class NotFound(val message: String = "Lyrics Not Available") : LyricsUiState
}

/**
 * Audio output device types for real-time routing detection.
 */
enum class AudioDeviceType {
    SPEAKER,
    BLUETOOTH,
    WIRED_HEADPHONES,
    USB_DAC,
    CHROMECAST,
    SYSTEM_DEFAULT,
    OTHER
}

@Immutable
data class ConnectedAudioDevice(
    val type: AudioDeviceType,
    val name: String,
    val description: String,
    val id: Int = 0,
    val address: String? = null,
    val isSystemDefault: Boolean = false
)

/**
 * Metadata for a playable audio track.
 */
@Immutable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverResId: Int? = null,
    val coverUrl: String? = null,
    val durationMs: Long,
    val audioQuality: String = "FLAC 24-bit / 96kHz",
    val primaryAmbientColor: Color = Color(0xFF263859),
    val secondaryAmbientColor: Color = Color(0xFF0B132B),
    val contentUri: String? = null,
    val contentId: Long = 0L,
    val albumArtUri: String? = null,
    val mimeType: String? = null,
    val filePath: String? = null,
    val sizeBytes: Long = 0L,
    val lyrics: List<LyricLine> = emptyList(),
    val sampleRateHz: Int = 44100,
    val bitDepth: Int = 16,
    val bitrateKbps: Int = 320,
    val formatName: String = "FLAC",
    val dateAdded: Long = 0L
) {
    companion object {
        val EMPTY = Track(
            id = "",
            title = "",
            artist = "",
            album = "",
            durationMs = 0L,
            primaryAmbientColor = Color.Unspecified,
            secondaryAmbientColor = Color.Unspecified
        )
    }
}

/**
 * Album data structure.
 */
@Immutable
data class Album(
    val name: String,
    val artist: String,
    val coverUrl: String? = null,
    val coverResId: Int? = null,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

/**
 * Artist data structure.
 */
@Immutable
data class Artist(
    val name: String,
    val trackCount: Int,
    val albumCount: Int,
    val bannerUrl: String? = null,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

/**
 * State representing the Home / Songs Library screen.
 */
@Immutable
data class HomeUiState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasMicrophonePermission: Boolean = false,
    val hasNearbyDevicesPermission: Boolean = false,
    val totalCount: Int = 0,
    val minDurationFilterSec: Int = 30,
    val blacklistedFoldersCount: Int = 0,
    val whitelistedFoldersCount: Int = 0
)

/**
 * State representing the primary playback UI.
 */
@Immutable
data class PlayerUiState(
    val currentTrack: Track,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val volume: Float = 0.72f,
    val activeDevice: ConnectedAudioDevice = ConnectedAudioDevice(
        type = AudioDeviceType.SPEAKER,
        name = "Device Speaker",
        description = "Internal Stereo Speakers"
    ),
    val isLyricsVisible: Boolean = false,
    val isNowPlayingExpanded: Boolean = false,
    val playlist: List<Track> = emptyList(),
    val currentTrackIndex: Int = 0,
    val queueCount: Int = 0,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val playbackSpeed: Float = 1.0f
)

/**
 * Equalizer band frequency and gain state.
 */
@Immutable
data class EqualizerBandState(
    val bandIndex: Short,
    val centerFreqHz: Int,
    val currentGainMb: Short,
    val minGainMb: Short,
    val maxGainMb: Short
)

/**
 * Equalizer UI and hardware configuration state.
 */
@Immutable
data class EqualizerUiState(
    val isSupported: Boolean = true,
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBandState> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPresetIndex: Short = -1,
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val reverbPreset: Short = 0,
    val loudnessGainMb: Int = 0,
    val pitchMultiplier: Float = 1.0f,
    val stereoBalance: Float = 0.0f,
    val useSystemEqualizer: Boolean = false
)

/**
 * App Theme Modes: System Default, Light, Dark, or Pure OLED Black
 */
enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    PURE_BLACK
}

/**
 * Customizable Player Layout styles for the Now Playing screen:
 * 1. APPLE_MUSIC (Glassmorphic blurred background, large edge-to-edge square card, linear progress)
 * 2. MATERIAL_YOU_EXPRESSIVE (Solid theme, squircle art, chunky row with 1.5x pill play/pause, pill action row)
 * 3. CIRCULAR_ORBIT (Smooth vertical gradient, circular vinyl art with circular progress arc wrapper)
 * 4. NEUMORPHIC_TACTILE (Soft UI dual-extrusion surfaces, recessed scrubber, embossed buttons)
 * 5. WAVEFORM_CYBER_STUDIO (Live audio frequency spectrum scrubber, digital telemetry & neon transport)
 * 6. VINTAGE_CASSETTE (Rotating twin tape spools, magnetic ribbon window, analog VU meter & tactile transport)
 */
enum class PlayerLayoutType(val displayName: String, val description: String) {
    M3_EXPRESSIVE(
        "Material 3 Expressive Studio",
        "Google's flagship expressive design: interactive shapes, motion physics, fluid wavy slider & dynamic sound visualizer"
    ),
    MATERIAL_YOU_EXPRESSIVE(
        "Material You Split-Card",
        "Dynamic theme cards, active device capsule, in-place lyrics & volume slider"
    ),
    APPLE_MUSIC(
        "Glassmorphic Apple Music",
        "Full-bleed dynamic blur mesh, frosted glass panels, synced lyrics & sleek volume slider"
    ),
    CIRCULAR_ORBIT(
        "Circular Orbit Turntable",
        "Spinning vinyl record disc with circular playback progress ring and tactile deck"
    ),
    WAVEFORM_CYBER_STUDIO(
        "Cyber Waveform Studio",
        "Live audio frequency spectrum scrubber, digital telemetry & neon transport"
    ),
    VINYL_TURNTABLE(
        "Hi-Fi Vinyl Turntable",
        "Realistic 33⅓ RPM rotating vinyl record with moving tonearm stylus & analog plinth deck"
    );

    // Backward-compatibility aliases
    companion object {
        val GLASSMORPHIC get() = APPLE_MUSIC
        val WAVEFORM get() = WAVEFORM_CYBER_STUDIO
        val CARD_SPLIT get() = MATERIAL_YOU_EXPRESSIVE
        val CIRCULAR_RADIAL get() = CIRCULAR_ORBIT
        val M3_EXPRESSIVE_LAYOUT get() = M3_EXPRESSIVE
    }
}

/**
 * Android Notification styles and action patterns for the foreground media service.
 */
enum class NotificationPattern(val displayName: String, val description: String) {
    STANDARD_MEDIA(
        "Standard Android Media",
        "Classic MediaStyle with Previous, Play/Pause, Next & system dismiss"
    ),
    EXTENDED_ACTIONS(
        "Expressive + Favorite Heart",
        "Previous, Play/Pause, Next, Favorite toggle & Close button"
    ),
    MINIMAL_COMPACT(
        "Minimal Compact",
        "Streamlined 2-button layout: Play/Pause and Next for zero clutter"
    ),
    DJ_SCRUBBER(
        "DJ Scrubbing (±10s)",
        "Rewind -10s, Play/Pause, Fast-Forward +10s, and Next track jump"
    ),
    SILENT_MEDIA_CONTROLS(
        "Silent Media Controls",
        "Full interactive playback controls in a completely silent, non-intrusive notification"
    )
}

/**
 * Pre-configured Color Palette themes for the entire application.
 */
enum class AppColorPalette(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    DYNAMIC("Dynamic Material You", 0xFF6750A4, 0xFF625B71),
    OCEAN_BLUE("Ocean Indigo", 0xFF3B82F6, 0xFF60A5FA),
    EMERALD_GREEN("Emerald Pine", 0xFF10B981, 0xFF34D399),
    CRIMSON_RED("Ruby Crimson", 0xFFEF4444, 0xFFF87171),
    SUNSET_AMBER("Sunset Amber", 0xFFF59E0B, 0xFFFBBF24),
    LAVENDER_PURPLE("Lavender Dream", 0xFF8B5CF6, 0xFFA78BFA),
    NEON_CYAN("Cyberpunk Cyan", 0xFF06B6D4, 0xFF22D3EE),
    ROSE_PINK("Rose Blush", 0xFFEC4899, 0xFFF472B6),
    GRAPHITE_SLATE("Graphite Slate", 0xFF64748B, 0xFF94A3B8)
}

/**
 * Always On Display (AOD) Layout options when music is playing.
 */
enum class AodLayoutType(val displayName: String, val description: String) {
    MINIMAL_OLED_CLOCK(
        "Minimalist OLED Clock",
        "Pure black power saver with dimmed glowing digital time & subtle track pulse"
    ),
    VINYL_GROOVE_ARC(
        "Vinyl Turntable Arc",
        "Minimal rotating vinyl groove ring & glowing tonearm indicator arc"
    ),
    CYBER_MATRIX_HUD(
        "Cyber Matrix HUD",
        "Sci-fi telemetry clock, cyan audio spectrum bars & Hi-Res bit-rate display"
    ),
    CAPSULE_PILL_GLOW(
        "Music Capsule Pill",
        "Floating neon pill with miniature album art, animated scrubber & time"
    )
}

/**
 * Material 3 Expressive Player Customization options.
 * Allows users to choose their artwork shape, scrubber style, hero button style,
 * motion physics profile, background treatment, and toggle any modules/widgets on screen.
 */
enum class M3ArtworkShape(val displayName: String, val description: String) {
    EXPRESSIVE_SQUIRCLE("Expressive Squircle", "Smooth continuous M3 curvature (32dp)"),
    SUPER_ELLIPSE("Superellipse", "Fluid 26dp modern squircle curve"),
    ORGANIC_CLOVER("Organic Clover", "Playful 4-petal scallop blossom flower shape"),
    HEXAGON_HIRES("Hexagonal Master", "6-sided faceted Hi-Res audio frame"),
    OCTAGON_FACET("Octagonal Facet", "8-sided cut-corner geometric container"),
    CURVED_ARCH("Curved Arch Dome", "Curved top dome with rounded base"),
    TICKET_NOTCH("Concert Ticket", "Curved side ticket notches with smooth corners"),
    DIAMOND_CUT("Diamond Facet", "18dp diamond cut-corner polygon card"),
    ROUNDED_SQUARE("Soft Rounded", "Classic 20dp rounded corner square"),
    VINYL_RECORD("Vinyl Disc", "Circular disc with subtle rotating vinyl grooves"),
    STARBURST_12("12-Point Starburst", "Playful expressive 12-point star contour"),
    STADIUM_PILL("Stadium Pill Capsule", "Elongated smooth stadium capsule"),
    ARCH_PORTAL("Sacred Arch Portal", "Gothic arched gateway dome"),
    TEARDROP_FLUID("Fluid Teardrop", "Organic fluid droplet with asymmetric corner"),
    SHIELD_HERALD("Hi-Res Shield Crest", "Faceted audio fidelity crest badge")
}

enum class M3ArtworkSize(val displayName: String, val description: String) {
    EXPANSIVE_HERO("Hero Large", "Dominant upper canvas album art"),
    BALANCED("Balanced Medium", "Harmonious proportions for controls and lyrics"),
    COMPACT("Compact Split", "Lower profile artwork with expanded control room")
}

enum class M3ScrubberStyle(val displayName: String, val description: String) {
    WAVY("Expressive Wavy Wave", "Undulating sinusoidal wave responding to playback"),
    CHUNKY_PILL("Tactile Chunky Pill", "Thick tactile track with dynamic spring thumb"),
    SEGMENTED_BEATS("Segmented Dashes", "Musical beat-sliced track dashes"),
    MINIMAL_SLIM("Minimal Sleek", "Ultra-thin modern hairline progress track"),
    DYNAMIC_SPECTRUM("Live Audio Spectrum", "Scrubber tracks live frequency amplitudes"),
    NEON_BEAM_GLOW("Neon Glow Beam", "Laser luminous beam with sparkling glow head"),
    VU_STEREO_TICKS("VU Stereo Meter Ticks", "Segmented LED volume ticks responding to tempo"),
    DOT_MATRIX_BEADS("Dot Matrix LED Beads", "Retro-futuristic dotted bead progress path"),
    GRADIENT_RIVER("Chroma Fluid River", "Multi-color gradient fluid ribbon with glowing thumb")
}

enum class M3HeroButtonStyle(val displayName: String, val description: String) {
    EXPRESSIVE_SQUIRCLE("Expressive Squircle", "Dynamic rounded squircle with tactile spring response"),
    PILL_FAB("Expressive Wide Pill", "Bold capsule container with prominent play/pause icon"),
    CIRCLE_HERO("Classic Circle FAB", "Clean circular elevated action button"),
    ASYMMETRIC_DIAMOND("Facet Diamond", "Modern geometric faceted polygon"),
    NEUMORPHIC_CONTOUR("Neumorphic Contour Glow", "Sculpted dual-layer embossed glow ring with tactile inner bevel"),
    DOUBLE_RING_ORBIT("Double Concentric Orbit", "Dual pulsating orbital aura rings surrounding hero play button"),
    ORGANIC_BLOSSOM("Organic Blossom Flower", "Playful 4-petal floral bloom shape"),
    HEXAGON_PRISM("Hexagonal Prism", "Faceted 6-sided Hi-Res audio prism button"),
    CAPSULE_SEGMENTED("Segmented Capsule Dock", "Split capsule transport dock with unified contour"),
    MINIMAL_OUTLINE_GLOW("Minimal Radiant Ring", "Hairline illuminated border ring with pulsating core"),
    STARBURST_HERO("Starburst Badge", "Expressive 8-point geometric star badge"),
    STADIUM_BADGE("Stadium Gradient Pill", "Rounded stadium pill with dynamic gradient accent")
}

enum class M3ControlButtonStyle(val displayName: String, val description: String) {
    EXPRESSIVE_TONAL_SQUIRCLE("Tonal Squircles", "Soft rounded square button containers"),
    PILL_SEGMENTED_DOCK("Unified Pill Dock", "Sleek conjoined pill bar for transport actions"),
    MINIMAL_FLOATING_ICONS("Minimal Clean Icons", "Airy frameless icons with dynamic ripple feedback"),
    GLOWING_CIRCULAR_PADS("Glowing Circular Badges", "Tonal circular pads with soft backdrop shadow"),
    FACETED_DIAMOND_TILES("Faceted Diamond Tiles", "Geometric sharp-corner polygon tiles")
}

enum class M3PlayerStyle(val displayName: String, val description: String) {
    EXPRESSIVE_DEFAULT("Dynamic Stage", "Harmonious upper artwork canvas with floating bottom dock"),
    VINYL_STUDIO_TURNTABLE("Vinyl Turntable Studio", "Rotating vinyl record with animated metal tonearm, vinyl grooves & RPM selector"),
    CARD_STAGE_STACK("3D Layered Card Stack", "Depth-elevated floating glass cards with dimensional shadow offsets"),
    IMMERSIVE_HERO_CANOPY("Immersive Hero Canopy", "Dominant full-width upper hero canopy with translucent floating pill overlay"),
    RETRO_CASSETTE_TAPE("Retro Cassette Deck", "Stylized cassette tape with rotating dual spools & analog tape counter"),
    STUDIO_CONSOLE_METERS("Audiophile Studio Console", "Multi-band live audio telemetry, stereo VU peak meters & Hi-Res specs"),
    MINIMAL_ESSENCE("Pure Minimalist Flow", "Ultra-clean typography and sleek gesture-friendly minimal player stage")
}

enum class M3ElementPlacement(val displayName: String, val description: String) {
    BALANCED_DEFAULT("Standard M3 Flow", "Artwork → Title/Artist → Scrubber → Playback Controls → Volume/Actions"),
    THUMB_FRIENDLY_BOTTOM("Bottom Ergonomic Dock", "Artwork → Scrubber → Title/Artist → Bottom-Anchored Controls"),
    CONTROLS_FIRST_TOP("Controls Above Scrubber", "Artwork → Title/Artist → Playback Controls → Scrubber → Volume"),
    COMPACT_SPLIT_CARDS("Split Multi-Deck Cards", "Side-by-side dual card decks grouping controls & specs"),
    EXPANDED_LYRICS_HERO("Lyrics-Centric Stage", "Prominent live synced lyrics card placed directly under artwork")
}

enum class M3InfoAlignment(val displayName: String, val description: String) {
    LEFT("Left Aligned", "Title & artist on the left with favorite heart on the right"),
    CENTER("Centered Hero", "Centered text with balanced symmetry across the stage"),
    SPLIT_ACCENT("Split Accent Badge", "Left title with decorative format badge tag")
}

enum class M3MotionProfile(val displayName: String, val description: String) {
    BOUNCY_ELASTIC("Elastic Spring Physics", "Rich spring tactile bounce with medium oscillation damping"),
    SNAPPY_DECEL("Snappy Deceleration", "Crisp instant response with rapid stabilization"),
    SMOOTH_HARMONIC("Smooth Harmonic Flow", "Gentle continuous breathing and fluid gliding dynamics")
}

enum class M3BackgroundStyle(val displayName: String, val description: String) {
    DYNAMIC_TONAL("Expressive Tonal Container", "Material 3 tonal surface colors dynamically matched to artwork"),
    AMBIENT_AURA("Ambient Radial Glow", "Vibrant glowing colorful ambient gradient aura"),
    GLASSMORPHIC_FROST("Frosted Glassmorphism", "Soft blurred backdrop with translucent glass cards"),
    AMOLED_BLACK("Pure AMOLED Black", "Deep battery-saving true black with crisp neon accents"),
    ORBITAL_NEBULA("Orbital Chroma Nebula", "Dual floating animated color plasma spheres"),
    MESH_AURORA_GLOW("Aurora Borealis Mesh", "Gentle swaying polychromatic wave mesh"),
    METALLIC_BRUSHED("Brushed Audiophile Slate", "Deep metallic textured finish with studio sheen")
}

enum class LyricsTextAlignment(val displayName: String) {
    LEFT("Left Aligned"),
    CENTER("Centered"),
    RIGHT("Right Aligned")
}

enum class LyricsBackdropStyle(val displayName: String, val description: String) {
    DYNAMIC_GAUSSIAN_BLUR("Dynamic Gaussian Blur", "Harmonic breathing artwork color aura with deep Gaussian blur"),
    ORBITAL_AURA("Orbital Chroma Aura", "Floating multi-node glowing color spheres behind lyrics"),
    MINIMAL_GRADIENT("Minimal Dark Gradient", "Subtle elegant dark gradient matching artwork tones"),
    PURE_AMOLED_BLACK("Pure AMOLED Black", "Deep pitch black background for maximum contrast and battery efficiency")
}

enum class LyricsTextSize(val displayName: String, val activeSp: Float, val inactiveSp: Float) {
    COMPACT("Compact", 20f, 18f),
    STANDARD("Standard", 24f, 22f),
    LARGE("Large (Sing)", 28f, 26f),
    EXPANSIVE("Expansive", 32f, 30f)
}

@Immutable
data class LyricsScreenConfig(
    val textSize: LyricsTextSize = LyricsTextSize.STANDARD,
    val textAlignment: LyricsTextAlignment = LyricsTextAlignment.LEFT,
    val inactiveTextContrast: Float = 0.80f, // 80% contrast
    val backdropStyle: LyricsBackdropStyle = LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR,
    val blurIntensity: Float = 0.85f,
    val karaokeWordFillEnabled: Boolean = true,
    val showMiniTransportDeck: Boolean = true,
    val autoScrollEnabled: Boolean = true,
    val tapToSeekEnabled: Boolean = true,
    val showAudioQualityBadge: Boolean = false,
    val hideControlsOnIdle: Boolean = true,
    val lyricGlowBloom: Boolean = true,
    val staggeredSlideIn: Boolean = true
)

@Immutable
data class M3ExpressivePlayerConfig(
    val playerStyle: M3PlayerStyle = M3PlayerStyle.EXPRESSIVE_DEFAULT,
    val artworkShape: M3ArtworkShape = M3ArtworkShape.EXPRESSIVE_SQUIRCLE,
    val artworkSize: M3ArtworkSize = M3ArtworkSize.EXPANSIVE_HERO,
    val scrubberStyle: M3ScrubberStyle = M3ScrubberStyle.WAVY,
    val heroButtonStyle: M3HeroButtonStyle = M3HeroButtonStyle.EXPRESSIVE_SQUIRCLE,
    val controlButtonStyle: M3ControlButtonStyle = M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE,
    val elementPlacement: M3ElementPlacement = M3ElementPlacement.BALANCED_DEFAULT,
    val infoAlignment: M3InfoAlignment = M3InfoAlignment.LEFT,
    val motionProfile: M3MotionProfile = M3MotionProfile.BOUNCY_ELASTIC,
    val backgroundStyle: M3BackgroundStyle = M3BackgroundStyle.DYNAMIC_TONAL,
    val auraGlowStrength: Float = 0.85f,
    val harmonicBreathingEnabled: Boolean = true,
    val showVisualizer: Boolean = true,
    val showAudioSpecs: Boolean = true,
    val showVolumeSlider: Boolean = true,
    val showDeviceCapsule: Boolean = true,
    val showActionPills: Boolean = true,
    val showShuffleRepeat: Boolean = true,
    val showFavoriteButton: Boolean = true,
    val showTrackDetails: Boolean = true,
    val showLyricsCard: Boolean = true,
    val expressiveHaptics: Boolean = true,
    val buttonGlowEffect: Boolean = true,
    val showVinylTonearm: Boolean = true,
    val showVuMeters: Boolean = true
)

/**
 * Persistent user settings for library, appearance, and audio.
 */
@Immutable
data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val colorPalette: AppColorPalette = AppColorPalette.DYNAMIC,
    val useDynamicColors: Boolean = true,
    val darkTheme: Boolean = true,
    val amoledBlack: Boolean = false,
    val dynamicColor: Boolean = true,
    val blurIntensity: Float = 0.8f,
    val forceHighRefreshRate: Boolean = true,
    val playerLayoutType: PlayerLayoutType = PlayerLayoutType.M3_EXPRESSIVE,
    val m3ExpressiveConfig: M3ExpressivePlayerConfig = M3ExpressivePlayerConfig(),
    val lyricsConfig: LyricsScreenConfig = LyricsScreenConfig(),
    val aodEnabled: Boolean = false,
    val aodLayoutType: AodLayoutType = AodLayoutType.MINIMAL_OLED_CLOCK,
    val aodKeepScreenAwake: Boolean = true,
    val aodBurnInProtection: Boolean = true,
    val aodBrightnessDim: Float = 0.25f,
    val notificationPattern: NotificationPattern = NotificationPattern.STANDARD_MEDIA,
    val silentNotification: Boolean = false,
    val minDurationSec: Int = 30,
    val blacklistedFolders: Set<String> = setOf("WhatsApp Audio", "Telegram Audio", "Voice Recorder"),
    val whitelistedFolders: Set<String> = emptySet(),
    val useWhitelistOnly: Boolean = false,
    val useSystemEqualizer: Boolean = false,
    val gaplessPlayback: Boolean = true,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDurationSec: Int = 3,
    val alwaysShuffle: Boolean = false,
    val showAudioDetails: Boolean = false,
    val lockScreenPlayer: Boolean = true,
    val musicDynamicTheming: Boolean = true,
    val changeFontsWithMusic: Boolean = true,
    val defaultFontFamily: String = "DEFAULT",
    val backdropBlurEnabled: Boolean = true,
    val glassmorphicEffectEnabled: Boolean = true,
    val snowballEffectEnabled: Boolean = true,
    val liveMovingGradientEnabled: Boolean = true,
    val homeScreenEffectsEnabled: Boolean = true,
    val homeScreenEffectStyle: String = "AMBIENT_AURA",
    val playbackVisualizerEnabled: Boolean = true,
    val waveformVisualizerStyle: String = "NEON_BARS",
    val librarySortMethod: String = "TITLE",
    val librarySortAscending: Boolean = true,
    val libraryTab: String = "SONGS"
)

/**
 * State for the background sleep timer.
 */
@Immutable
data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val isEndOfTrack: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

