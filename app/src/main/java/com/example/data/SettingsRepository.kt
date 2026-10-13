package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.PlayerLayoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixel_player_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        val FORCE_HIGH_REFRESH_RATE = booleanPreferencesKey("force_high_refresh_rate")
        val PLAYER_LAYOUT_TYPE = stringPreferencesKey("player_layout_type")
        val MIN_DURATION_SEC = intPreferencesKey("min_duration_sec")
        val BLACKLISTED_FOLDERS = stringSetPreferencesKey("blacklisted_folders")
        val WHITELISTED_FOLDERS = stringSetPreferencesKey("whitelisted_folders")
        val USE_WHITELIST_ONLY = booleanPreferencesKey("use_whitelist_only")
        val USE_SYSTEM_EQUALIZER = booleanPreferencesKey("use_system_equalizer")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
        val CROSSFADE_DURATION_SEC = intPreferencesKey("crossfade_duration_sec")
        val ALWAYS_SHUFFLE = booleanPreferencesKey("always_shuffle")
        val SHOW_AUDIO_DETAILS = booleanPreferencesKey("show_audio_details")
        val LOCK_SCREEN_PLAYER = booleanPreferencesKey("lock_screen_player")
        val MUSIC_DYNAMIC_THEMING = booleanPreferencesKey("music_dynamic_theming")
        val CHANGE_FONTS_WITH_MUSIC = booleanPreferencesKey("change_fonts_with_music")
        val DEFAULT_FONT_FAMILY = stringPreferencesKey("default_font_family")
        val BACKDROP_BLUR_ENABLED = booleanPreferencesKey("backdrop_blur_enabled")
        val GLASSMORPHIC_EFFECT_ENABLED = booleanPreferencesKey("glassmorphic_effect_enabled")
        val SNOWBALL_EFFECT_ENABLED = booleanPreferencesKey("snowball_effect_enabled")
        val LIVE_MOVING_GRADIENT_ENABLED = booleanPreferencesKey("live_moving_gradient_enabled")
        val HOME_SCREEN_EFFECTS_ENABLED = booleanPreferencesKey("home_screen_effects_enabled")
        val HOME_SCREEN_EFFECT_STYLE = stringPreferencesKey("home_screen_effect_style")
        val PLAYBACK_VISUALIZER_ENABLED = booleanPreferencesKey("playback_visualizer_enabled")
        val WAVEFORM_VISUALIZER_STYLE = stringPreferencesKey("waveform_visualizer_style")
        val NOTIFICATION_PATTERN = stringPreferencesKey("notification_pattern")
        val SILENT_NOTIFICATION = booleanPreferencesKey("silent_notification")
        val LIBRARY_SORT_METHOD = stringPreferencesKey("library_sort_method")
        val LIBRARY_SORT_ASCENDING = booleanPreferencesKey("library_sort_ascending")
        val LIBRARY_TAB = stringPreferencesKey("library_tab")
        val AOD_ENABLED = booleanPreferencesKey("aod_enabled")
        val AOD_LAYOUT_TYPE = stringPreferencesKey("aod_layout_type")
        val AOD_KEEP_SCREEN_AWAKE = booleanPreferencesKey("aod_keep_screen_awake")
        val AOD_BURN_IN_PROTECTION = booleanPreferencesKey("aod_burn_in_protection")
        val AOD_BRIGHTNESS_DIM = floatPreferencesKey("aod_brightness_dim")

        // Material 3 Expressive Player Customization Keys
        val M3_PLAYER_STYLE = stringPreferencesKey("m3_player_style")
        val M3_ARTWORK_SHAPE = stringPreferencesKey("m3_artwork_shape")
        val M3_ARTWORK_SIZE = stringPreferencesKey("m3_artwork_size")
        val M3_SCRUBBER_STYLE = stringPreferencesKey("m3_scrubber_style")
        val M3_HERO_BUTTON_STYLE = stringPreferencesKey("m3_hero_button_style")
        val M3_CONTROL_BUTTON_STYLE = stringPreferencesKey("m3_control_button_style")
        val M3_ELEMENT_PLACEMENT = stringPreferencesKey("m3_element_placement")
        val M3_INFO_ALIGNMENT = stringPreferencesKey("m3_info_alignment")
        val M3_BACKGROUND_STYLE = stringPreferencesKey("m3_background_style")
        val M3_SHOW_VISUALIZER = booleanPreferencesKey("m3_show_visualizer")
        val M3_SHOW_AUDIO_SPECS = booleanPreferencesKey("m3_show_audio_specs")
        val M3_SHOW_VOLUME_SLIDER = booleanPreferencesKey("m3_show_volume_slider")
        val M3_SHOW_DEVICE_CAPSULE = booleanPreferencesKey("m3_show_device_capsule")
        val M3_SHOW_ACTION_PILLS = booleanPreferencesKey("m3_show_action_pills")
        val M3_SHOW_SHUFFLE_REPEAT = booleanPreferencesKey("m3_show_shuffle_repeat")
        val M3_SHOW_FAVORITE_BUTTON = booleanPreferencesKey("m3_show_favorite_button")
        val M3_SHOW_TRACK_DETAILS = booleanPreferencesKey("m3_show_track_details")
        val M3_SHOW_LYRICS_CARD = booleanPreferencesKey("m3_show_lyrics_card")
        val M3_EXPRESSIVE_HAPTICS = booleanPreferencesKey("m3_expressive_haptics")
        val M3_BUTTON_GLOW = booleanPreferencesKey("m3_button_glow")
        val M3_SHOW_VINYL_TONEARM = booleanPreferencesKey("m3_show_vinyl_tonearm")
        val M3_SHOW_VU_METERS = booleanPreferencesKey("m3_show_vu_meters")

        // Full-Screen Lyrics Customization Keys
        val LYRICS_TEXT_SIZE = stringPreferencesKey("lyrics_text_size")
        val LYRICS_TEXT_ALIGNMENT = stringPreferencesKey("lyrics_text_alignment")
        val LYRICS_INACTIVE_CONTRAST = floatPreferencesKey("lyrics_inactive_contrast")
        val LYRICS_BACKDROP_STYLE = stringPreferencesKey("lyrics_backdrop_style")
        val LYRICS_BLUR_INTENSITY = floatPreferencesKey("lyrics_blur_intensity")
        val LYRICS_KARAOKE_FILL = booleanPreferencesKey("lyrics_karaoke_fill")
        val LYRICS_SHOW_MINI_TRANSPORT = booleanPreferencesKey("lyrics_show_mini_transport")
        val LYRICS_AUTO_SCROLL = booleanPreferencesKey("lyrics_auto_scroll")
        val LYRICS_TAP_TO_SEEK = booleanPreferencesKey("lyrics_tap_to_seek")
        val LYRICS_SHOW_AUDIO_BADGE = booleanPreferencesKey("lyrics_show_audio_badge")
        val LYRICS_GLOW_BLOOM = booleanPreferencesKey("lyrics_glow_bloom")
        val LYRICS_STAGGERED_SLIDE_IN = booleanPreferencesKey("lyrics_staggered_slide_in")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val layoutTypeStr = preferences[PreferencesKeys.PLAYER_LAYOUT_TYPE]
        val layoutType = try {
            if (layoutTypeStr != null) PlayerLayoutType.valueOf(layoutTypeStr) else PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE
        } catch (e: Exception) {
            PlayerLayoutType.MATERIAL_YOU_EXPRESSIVE
        }

        val m3Config = com.example.model.M3ExpressivePlayerConfig(
            playerStyle = try {
                preferences[PreferencesKeys.M3_PLAYER_STYLE]?.let { com.example.model.M3PlayerStyle.valueOf(it) } ?: com.example.model.M3PlayerStyle.EXPRESSIVE_DEFAULT
            } catch (e: Exception) { com.example.model.M3PlayerStyle.EXPRESSIVE_DEFAULT },
            artworkShape = try {
                preferences[PreferencesKeys.M3_ARTWORK_SHAPE]?.let { com.example.model.M3ArtworkShape.valueOf(it) } ?: com.example.model.M3ArtworkShape.EXPRESSIVE_SQUIRCLE
            } catch (e: Exception) { com.example.model.M3ArtworkShape.EXPRESSIVE_SQUIRCLE },
            artworkSize = try {
                preferences[PreferencesKeys.M3_ARTWORK_SIZE]?.let { com.example.model.M3ArtworkSize.valueOf(it) } ?: com.example.model.M3ArtworkSize.EXPANSIVE_HERO
            } catch (e: Exception) { com.example.model.M3ArtworkSize.EXPANSIVE_HERO },
            scrubberStyle = try {
                preferences[PreferencesKeys.M3_SCRUBBER_STYLE]?.let { com.example.model.M3ScrubberStyle.valueOf(it) } ?: com.example.model.M3ScrubberStyle.WAVY
            } catch (e: Exception) { com.example.model.M3ScrubberStyle.WAVY },
            heroButtonStyle = try {
                preferences[PreferencesKeys.M3_HERO_BUTTON_STYLE]?.let { com.example.model.M3HeroButtonStyle.valueOf(it) } ?: com.example.model.M3HeroButtonStyle.EXPRESSIVE_SQUIRCLE
            } catch (e: Exception) { com.example.model.M3HeroButtonStyle.EXPRESSIVE_SQUIRCLE },
            controlButtonStyle = try {
                preferences[PreferencesKeys.M3_CONTROL_BUTTON_STYLE]?.let { com.example.model.M3ControlButtonStyle.valueOf(it) } ?: com.example.model.M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE
            } catch (e: Exception) { com.example.model.M3ControlButtonStyle.EXPRESSIVE_TONAL_SQUIRCLE },
            elementPlacement = try {
                preferences[PreferencesKeys.M3_ELEMENT_PLACEMENT]?.let { com.example.model.M3ElementPlacement.valueOf(it) } ?: com.example.model.M3ElementPlacement.BALANCED_DEFAULT
            } catch (e: Exception) { com.example.model.M3ElementPlacement.BALANCED_DEFAULT },
            infoAlignment = try {
                preferences[PreferencesKeys.M3_INFO_ALIGNMENT]?.let { com.example.model.M3InfoAlignment.valueOf(it) } ?: com.example.model.M3InfoAlignment.LEFT
            } catch (e: Exception) { com.example.model.M3InfoAlignment.LEFT },
            backgroundStyle = try {
                preferences[PreferencesKeys.M3_BACKGROUND_STYLE]?.let { com.example.model.M3BackgroundStyle.valueOf(it) } ?: com.example.model.M3BackgroundStyle.DYNAMIC_TONAL
            } catch (e: Exception) { com.example.model.M3BackgroundStyle.DYNAMIC_TONAL },
            showVisualizer = preferences[PreferencesKeys.M3_SHOW_VISUALIZER] ?: true,
            showAudioSpecs = preferences[PreferencesKeys.M3_SHOW_AUDIO_SPECS] ?: true,
            showVolumeSlider = preferences[PreferencesKeys.M3_SHOW_VOLUME_SLIDER] ?: true,
            showDeviceCapsule = preferences[PreferencesKeys.M3_SHOW_DEVICE_CAPSULE] ?: true,
            showActionPills = preferences[PreferencesKeys.M3_SHOW_ACTION_PILLS] ?: true,
            showShuffleRepeat = preferences[PreferencesKeys.M3_SHOW_SHUFFLE_REPEAT] ?: true,
            showFavoriteButton = preferences[PreferencesKeys.M3_SHOW_FAVORITE_BUTTON] ?: true,
            showTrackDetails = preferences[PreferencesKeys.M3_SHOW_TRACK_DETAILS] ?: true,
            showLyricsCard = preferences[PreferencesKeys.M3_SHOW_LYRICS_CARD] ?: true,
            expressiveHaptics = preferences[PreferencesKeys.M3_EXPRESSIVE_HAPTICS] ?: true,
            buttonGlowEffect = preferences[PreferencesKeys.M3_BUTTON_GLOW] ?: true,
            showVinylTonearm = preferences[PreferencesKeys.M3_SHOW_VINYL_TONEARM] ?: true,
            showVuMeters = preferences[PreferencesKeys.M3_SHOW_VU_METERS] ?: true
        )

        val lyricsConfig = com.example.model.LyricsScreenConfig(
            textSize = try {
                preferences[PreferencesKeys.LYRICS_TEXT_SIZE]?.let { com.example.model.LyricsTextSize.valueOf(it) } ?: com.example.model.LyricsTextSize.STANDARD
            } catch (e: Exception) { com.example.model.LyricsTextSize.STANDARD },
            textAlignment = try {
                preferences[PreferencesKeys.LYRICS_TEXT_ALIGNMENT]?.let { com.example.model.LyricsTextAlignment.valueOf(it) } ?: com.example.model.LyricsTextAlignment.LEFT
            } catch (e: Exception) { com.example.model.LyricsTextAlignment.LEFT },
            inactiveTextContrast = preferences[PreferencesKeys.LYRICS_INACTIVE_CONTRAST] ?: 0.80f,
            backdropStyle = try {
                preferences[PreferencesKeys.LYRICS_BACKDROP_STYLE]?.let { com.example.model.LyricsBackdropStyle.valueOf(it) } ?: com.example.model.LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR
            } catch (e: Exception) { com.example.model.LyricsBackdropStyle.DYNAMIC_GAUSSIAN_BLUR },
            blurIntensity = preferences[PreferencesKeys.LYRICS_BLUR_INTENSITY] ?: 0.85f,
            karaokeWordFillEnabled = preferences[PreferencesKeys.LYRICS_KARAOKE_FILL] ?: true,
            showMiniTransportDeck = preferences[PreferencesKeys.LYRICS_SHOW_MINI_TRANSPORT] ?: true,
            autoScrollEnabled = preferences[PreferencesKeys.LYRICS_AUTO_SCROLL] ?: true,
            tapToSeekEnabled = preferences[PreferencesKeys.LYRICS_TAP_TO_SEEK] ?: true,
            showAudioQualityBadge = preferences[PreferencesKeys.LYRICS_SHOW_AUDIO_BADGE] ?: false,
            lyricGlowBloom = preferences[PreferencesKeys.LYRICS_GLOW_BLOOM] ?: true,
            staggeredSlideIn = preferences[PreferencesKeys.LYRICS_STAGGERED_SLIDE_IN] ?: true
        )

        val aodLayoutTypeStr = preferences[PreferencesKeys.AOD_LAYOUT_TYPE]
        val aodLayoutType = try {
            if (aodLayoutTypeStr != null) com.example.model.AodLayoutType.valueOf(aodLayoutTypeStr) else com.example.model.AodLayoutType.MINIMAL_OLED_CLOCK
        } catch (e: Exception) {
            com.example.model.AodLayoutType.MINIMAL_OLED_CLOCK
        }

        val aodEnabled = preferences[PreferencesKeys.AOD_ENABLED] ?: false
        val aodKeepScreenAwake = preferences[PreferencesKeys.AOD_KEEP_SCREEN_AWAKE] ?: true
        val aodBurnInProtection = preferences[PreferencesKeys.AOD_BURN_IN_PROTECTION] ?: true
        val aodBrightnessDim = preferences[PreferencesKeys.AOD_BRIGHTNESS_DIM] ?: 0.25f

        val notificationPatternStr = preferences[PreferencesKeys.NOTIFICATION_PATTERN]
        val notificationPattern = try {
            if (notificationPatternStr != null) com.example.model.NotificationPattern.valueOf(notificationPatternStr) else com.example.model.NotificationPattern.STANDARD_MEDIA
        } catch (e: Exception) {
            com.example.model.NotificationPattern.STANDARD_MEDIA
        }

        val silentNotification = preferences[PreferencesKeys.SILENT_NOTIFICATION] ?: false

        val themeModeStr = preferences[PreferencesKeys.THEME_MODE]
        val legacyDark = preferences[PreferencesKeys.DARK_THEME] ?: true
        val legacyAmoled = preferences[PreferencesKeys.AMOLED_BLACK] ?: false
        val themeMode = try {
            if (themeModeStr != null) {
                AppThemeMode.valueOf(themeModeStr)
            } else if (legacyAmoled) {
                AppThemeMode.PURE_BLACK
            } else if (legacyDark) {
                AppThemeMode.DARK
            } else {
                AppThemeMode.LIGHT
            }
        } catch (e: Exception) {
            AppThemeMode.DARK
        }

        val useDynamicColors = preferences[PreferencesKeys.USE_DYNAMIC_COLORS]
            ?: preferences[PreferencesKeys.DYNAMIC_COLOR]
            ?: true

        val isDark = themeMode == AppThemeMode.DARK || themeMode == AppThemeMode.PURE_BLACK
        val isAmoled = themeMode == AppThemeMode.PURE_BLACK

        val colorPaletteStr = preferences[PreferencesKeys.COLOR_PALETTE]
        val colorPalette = try {
            if (colorPaletteStr != null) com.example.model.AppColorPalette.valueOf(colorPaletteStr) else com.example.model.AppColorPalette.DYNAMIC
        } catch (e: Exception) {
            com.example.model.AppColorPalette.DYNAMIC
        }

        val appSettings = AppSettings(
            themeMode = themeMode,
            colorPalette = colorPalette,
            useDynamicColors = useDynamicColors,
            darkTheme = isDark,
            amoledBlack = isAmoled,
            dynamicColor = useDynamicColors,
            blurIntensity = preferences[PreferencesKeys.BLUR_INTENSITY] ?: 0.8f,
            forceHighRefreshRate = preferences[PreferencesKeys.FORCE_HIGH_REFRESH_RATE] ?: true,
            playerLayoutType = layoutType,
            m3ExpressiveConfig = m3Config,
            lyricsConfig = lyricsConfig,
            aodEnabled = aodEnabled,
            aodLayoutType = aodLayoutType,
            aodKeepScreenAwake = aodKeepScreenAwake,
            aodBurnInProtection = aodBurnInProtection,
            aodBrightnessDim = aodBrightnessDim,
            notificationPattern = notificationPattern,
            silentNotification = silentNotification,
            minDurationSec = preferences[PreferencesKeys.MIN_DURATION_SEC] ?: 30,
            blacklistedFolders = preferences[PreferencesKeys.BLACKLISTED_FOLDERS] ?: setOf("WhatsApp Audio", "Telegram Audio", "Voice Recorder"),
            whitelistedFolders = preferences[PreferencesKeys.WHITELISTED_FOLDERS] ?: emptySet(),
            useWhitelistOnly = preferences[PreferencesKeys.USE_WHITELIST_ONLY] ?: false,
            useSystemEqualizer = preferences[PreferencesKeys.USE_SYSTEM_EQUALIZER] ?: false,
            gaplessPlayback = preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
            crossfadeEnabled = preferences[PreferencesKeys.CROSSFADE_ENABLED] ?: false,
            crossfadeDurationSec = preferences[PreferencesKeys.CROSSFADE_DURATION_SEC] ?: 3,
            alwaysShuffle = preferences[PreferencesKeys.ALWAYS_SHUFFLE] ?: false,
            showAudioDetails = preferences[PreferencesKeys.SHOW_AUDIO_DETAILS] ?: false,
            lockScreenPlayer = preferences[PreferencesKeys.LOCK_SCREEN_PLAYER] ?: true,
            musicDynamicTheming = preferences[PreferencesKeys.MUSIC_DYNAMIC_THEMING] ?: true,
            changeFontsWithMusic = preferences[PreferencesKeys.CHANGE_FONTS_WITH_MUSIC] ?: true,
            defaultFontFamily = preferences[PreferencesKeys.DEFAULT_FONT_FAMILY] ?: "DEFAULT",
            backdropBlurEnabled = preferences[PreferencesKeys.BACKDROP_BLUR_ENABLED] ?: true,
            glassmorphicEffectEnabled = preferences[PreferencesKeys.GLASSMORPHIC_EFFECT_ENABLED]
                ?: preferences[PreferencesKeys.BACKDROP_BLUR_ENABLED]
                ?: true,
            snowballEffectEnabled = preferences[PreferencesKeys.SNOWBALL_EFFECT_ENABLED] ?: true,
            liveMovingGradientEnabled = preferences[PreferencesKeys.LIVE_MOVING_GRADIENT_ENABLED] ?: true,
            homeScreenEffectsEnabled = preferences[PreferencesKeys.HOME_SCREEN_EFFECTS_ENABLED] ?: true,
            homeScreenEffectStyle = preferences[PreferencesKeys.HOME_SCREEN_EFFECT_STYLE] ?: "AMBIENT_AURA",
            playbackVisualizerEnabled = preferences[PreferencesKeys.PLAYBACK_VISUALIZER_ENABLED] ?: true,
            waveformVisualizerStyle = preferences[PreferencesKeys.WAVEFORM_VISUALIZER_STYLE] ?: "NEON_BARS",
            librarySortMethod = preferences[PreferencesKeys.LIBRARY_SORT_METHOD] ?: "TITLE",
            librarySortAscending = preferences[PreferencesKeys.LIBRARY_SORT_ASCENDING] ?: true,
            libraryTab = preferences[PreferencesKeys.LIBRARY_TAB] ?: "SONGS"
        )
        persistFastCache(appSettings)
        appSettings
    }

    /**
     * Instantly reads the cached settings synchronously on startup to prevent whitelist race conditions.
     */
    fun getSettingsFastSync(): AppSettings {
        return try {
            val sp = context.getSharedPreferences("pixel_player_fast_settings", Context.MODE_PRIVATE)
            val whitelist = sp.getStringSet("whitelisted_folders", null) ?: emptySet()
            val blacklist = sp.getStringSet("blacklisted_folders", null) ?: setOf("WhatsApp Audio", "Telegram Audio", "Voice Recorder")
            val useWhitelistOnly = sp.getBoolean("use_whitelist_only", false)
            val minDuration = sp.getInt("min_duration_sec", 30)
            val themeModeStr = sp.getString("theme_mode", null)
            val themeMode = try {
                if (themeModeStr != null) AppThemeMode.valueOf(themeModeStr) else AppThemeMode.DARK
            } catch (_: Throwable) { AppThemeMode.DARK }

            AppSettings(
                themeMode = themeMode,
                darkTheme = themeMode == AppThemeMode.DARK || themeMode == AppThemeMode.PURE_BLACK,
                amoledBlack = themeMode == AppThemeMode.PURE_BLACK,
                whitelistedFolders = whitelist,
                blacklistedFolders = blacklist,
                useWhitelistOnly = useWhitelistOnly,
                minDurationSec = minDuration
            )
        } catch (_: Throwable) {
            AppSettings()
        }
    }

    private fun persistFastCache(settings: AppSettings) {
        try {
            context.getSharedPreferences("pixel_player_fast_settings", Context.MODE_PRIVATE)
                .edit()
                .putStringSet("whitelisted_folders", settings.whitelistedFolders)
                .putStringSet("blacklisted_folders", settings.blacklistedFolders)
                .putBoolean("use_whitelist_only", settings.useWhitelistOnly)
                .putInt("min_duration_sec", settings.minDurationSec)
                .putString("theme_mode", settings.themeMode.name)
                .apply()
        } catch (_: Throwable) {}
    }

    suspend fun setNotificationPattern(pattern: com.example.model.NotificationPattern) {
        context.dataStore.edit {
            it[PreferencesKeys.NOTIFICATION_PATTERN] = pattern.name
        }
    }

    suspend fun setSilentNotification(silent: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.SILENT_NOTIFICATION] = silent
        }
    }

    suspend fun setColorPalette(palette: com.example.model.AppColorPalette) {
        context.dataStore.edit {
            it[PreferencesKeys.COLOR_PALETTE] = palette.name
            if (palette != com.example.model.AppColorPalette.DYNAMIC) {
                it[PreferencesKeys.USE_DYNAMIC_COLORS] = false
                it[PreferencesKeys.DYNAMIC_COLOR] = false
            }
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
            when (mode) {
                AppThemeMode.PURE_BLACK -> {
                    prefs[PreferencesKeys.DARK_THEME] = true
                    prefs[PreferencesKeys.AMOLED_BLACK] = true
                }
                AppThemeMode.DARK -> {
                    prefs[PreferencesKeys.DARK_THEME] = true
                    prefs[PreferencesKeys.AMOLED_BLACK] = false
                }
                AppThemeMode.LIGHT -> {
                    prefs[PreferencesKeys.DARK_THEME] = false
                    prefs[PreferencesKeys.AMOLED_BLACK] = false
                }
                AppThemeMode.SYSTEM -> {
                    prefs[PreferencesKeys.DARK_THEME] = true
                    prefs[PreferencesKeys.AMOLED_BLACK] = false
                }
            }
        }
    }

    suspend fun setUseDynamicColors(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.USE_DYNAMIC_COLORS] = enabled
            it[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setPlayerLayoutType(layoutType: PlayerLayoutType) {
        context.dataStore.edit { it[PreferencesKeys.PLAYER_LAYOUT_TYPE] = layoutType.name }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMOLED_BLACK] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setBlurIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.BLUR_INTENSITY] = intensity }
    }

    suspend fun setForceHighRefreshRate(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FORCE_HIGH_REFRESH_RATE] = enabled }
    }

    suspend fun setMinDurationSec(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.MIN_DURATION_SEC] = seconds }
    }

    suspend fun addBlacklistedFolder(folderName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.BLACKLISTED_FOLDERS] ?: setOf("WhatsApp Audio", "Telegram Audio", "Voice Recorder")
            preferences[PreferencesKeys.BLACKLISTED_FOLDERS] = current + folderName
        }
    }

    suspend fun removeBlacklistedFolder(folderName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.BLACKLISTED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.BLACKLISTED_FOLDERS] = current - folderName
        }
    }

    suspend fun addWhitelistedFolder(folderUriOrPath: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.WHITELISTED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.WHITELISTED_FOLDERS] = current + folderUriOrPath
        }
    }

    suspend fun removeWhitelistedFolder(folderUriOrPath: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.WHITELISTED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.WHITELISTED_FOLDERS] = current - folderUriOrPath
        }
    }

    suspend fun setUseWhitelistOnly(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.USE_WHITELIST_ONLY] = enabled }
    }

    suspend fun setUseSystemEqualizer(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.USE_SYSTEM_EQUALIZER] = enabled }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CROSSFADE_ENABLED] = enabled }
    }

    suspend fun setCrossfadeDurationSec(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.CROSSFADE_DURATION_SEC] = seconds }
    }

    suspend fun setAlwaysShuffle(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ALWAYS_SHUFFLE] = enabled }
    }

    suspend fun setShowAudioDetails(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_AUDIO_DETAILS] = enabled }
    }

    suspend fun setLockScreenPlayer(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.LOCK_SCREEN_PLAYER] = enabled }
    }

    suspend fun setMusicDynamicTheming(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MUSIC_DYNAMIC_THEMING] = enabled }
    }

    suspend fun setChangeFontsWithMusic(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CHANGE_FONTS_WITH_MUSIC] = enabled }
    }

    suspend fun setDefaultFontFamily(fontFamily: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_FONT_FAMILY] = fontFamily }
    }

    suspend fun setBackdropBlurEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.BACKDROP_BLUR_ENABLED] = enabled
            it[PreferencesKeys.GLASSMORPHIC_EFFECT_ENABLED] = enabled
        }
    }

    suspend fun setGlassmorphicEffectEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.GLASSMORPHIC_EFFECT_ENABLED] = enabled
            it[PreferencesKeys.BACKDROP_BLUR_ENABLED] = enabled
        }
    }

    suspend fun setSnowballEffectEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.SNOWBALL_EFFECT_ENABLED] = enabled
        }
    }

    suspend fun setLiveMovingGradientEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.LIVE_MOVING_GRADIENT_ENABLED] = enabled
        }
    }

    suspend fun setHomeScreenEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.HOME_SCREEN_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setHomeScreenEffectStyle(style: String) {
        context.dataStore.edit {
            it[PreferencesKeys.HOME_SCREEN_EFFECT_STYLE] = style
        }
    }

    suspend fun setPlaybackVisualizerEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.PLAYBACK_VISUALIZER_ENABLED] = enabled
        }
    }

    suspend fun setWaveformVisualizerStyle(styleName: String) {
        context.dataStore.edit {
            it[PreferencesKeys.WAVEFORM_VISUALIZER_STYLE] = styleName
        }
    }

    suspend fun setLibrarySort(method: String, ascending: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.LIBRARY_SORT_METHOD] = method
            it[PreferencesKeys.LIBRARY_SORT_ASCENDING] = ascending
        }
    }

    suspend fun setLibraryTab(tab: String) {
        context.dataStore.edit {
            it[PreferencesKeys.LIBRARY_TAB] = tab
        }
    }

    suspend fun setAodEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AOD_ENABLED] = enabled
        }
    }

    suspend fun setAodLayoutType(layoutType: com.example.model.AodLayoutType) {
        context.dataStore.edit {
            it[PreferencesKeys.AOD_LAYOUT_TYPE] = layoutType.name
        }
    }

    suspend fun setAodKeepScreenAwake(awake: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AOD_KEEP_SCREEN_AWAKE] = awake
        }
    }

    suspend fun setAodBurnInProtection(protection: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.AOD_BURN_IN_PROTECTION] = protection
        }
    }

    suspend fun setAodBrightnessDim(dim: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.AOD_BRIGHTNESS_DIM] = dim
        }
    }

    suspend fun updateM3ExpressiveConfig(config: com.example.model.M3ExpressivePlayerConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.M3_PLAYER_STYLE] = config.playerStyle.name
            preferences[PreferencesKeys.M3_ARTWORK_SHAPE] = config.artworkShape.name
            preferences[PreferencesKeys.M3_ARTWORK_SIZE] = config.artworkSize.name
            preferences[PreferencesKeys.M3_SCRUBBER_STYLE] = config.scrubberStyle.name
            preferences[PreferencesKeys.M3_HERO_BUTTON_STYLE] = config.heroButtonStyle.name
            preferences[PreferencesKeys.M3_CONTROL_BUTTON_STYLE] = config.controlButtonStyle.name
            preferences[PreferencesKeys.M3_ELEMENT_PLACEMENT] = config.elementPlacement.name
            preferences[PreferencesKeys.M3_INFO_ALIGNMENT] = config.infoAlignment.name
            preferences[PreferencesKeys.M3_BACKGROUND_STYLE] = config.backgroundStyle.name
            preferences[PreferencesKeys.M3_SHOW_VISUALIZER] = config.showVisualizer
            preferences[PreferencesKeys.M3_SHOW_AUDIO_SPECS] = config.showAudioSpecs
            preferences[PreferencesKeys.M3_SHOW_VOLUME_SLIDER] = config.showVolumeSlider
            preferences[PreferencesKeys.M3_SHOW_DEVICE_CAPSULE] = config.showDeviceCapsule
            preferences[PreferencesKeys.M3_SHOW_ACTION_PILLS] = config.showActionPills
            preferences[PreferencesKeys.M3_SHOW_SHUFFLE_REPEAT] = config.showShuffleRepeat
            preferences[PreferencesKeys.M3_SHOW_FAVORITE_BUTTON] = config.showFavoriteButton
            preferences[PreferencesKeys.M3_SHOW_TRACK_DETAILS] = config.showTrackDetails
            preferences[PreferencesKeys.M3_SHOW_LYRICS_CARD] = config.showLyricsCard
            preferences[PreferencesKeys.M3_EXPRESSIVE_HAPTICS] = config.expressiveHaptics
            preferences[PreferencesKeys.M3_BUTTON_GLOW] = config.buttonGlowEffect
            preferences[PreferencesKeys.M3_SHOW_VINYL_TONEARM] = config.showVinylTonearm
            preferences[PreferencesKeys.M3_SHOW_VU_METERS] = config.showVuMeters
        }
    }

    suspend fun updateLyricsConfig(config: com.example.model.LyricsScreenConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LYRICS_TEXT_SIZE] = config.textSize.name
            preferences[PreferencesKeys.LYRICS_TEXT_ALIGNMENT] = config.textAlignment.name
            preferences[PreferencesKeys.LYRICS_INACTIVE_CONTRAST] = config.inactiveTextContrast
            preferences[PreferencesKeys.LYRICS_BACKDROP_STYLE] = config.backdropStyle.name
            preferences[PreferencesKeys.LYRICS_BLUR_INTENSITY] = config.blurIntensity
            preferences[PreferencesKeys.LYRICS_KARAOKE_FILL] = config.karaokeWordFillEnabled
            preferences[PreferencesKeys.LYRICS_SHOW_MINI_TRANSPORT] = config.showMiniTransportDeck
            preferences[PreferencesKeys.LYRICS_AUTO_SCROLL] = config.autoScrollEnabled
            preferences[PreferencesKeys.LYRICS_TAP_TO_SEEK] = config.tapToSeekEnabled
            preferences[PreferencesKeys.LYRICS_SHOW_AUDIO_BADGE] = config.showAudioQualityBadge
            preferences[PreferencesKeys.LYRICS_GLOW_BLOOM] = config.lyricGlowBloom
            preferences[PreferencesKeys.LYRICS_STAGGERED_SLIDE_IN] = config.staggeredSlideIn
        }
    }

    suspend fun restoreAllSettings(s: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = s.themeMode.name
            preferences[PreferencesKeys.COLOR_PALETTE] = s.colorPalette.name
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = s.useDynamicColors
            preferences[PreferencesKeys.DARK_THEME] = s.darkTheme
            preferences[PreferencesKeys.AMOLED_BLACK] = s.amoledBlack
            preferences[PreferencesKeys.DYNAMIC_COLOR] = s.dynamicColor
            preferences[PreferencesKeys.BLUR_INTENSITY] = s.blurIntensity
            preferences[PreferencesKeys.FORCE_HIGH_REFRESH_RATE] = s.forceHighRefreshRate
            preferences[PreferencesKeys.PLAYER_LAYOUT_TYPE] = s.playerLayoutType.name
            preferences[PreferencesKeys.MIN_DURATION_SEC] = s.minDurationSec
            preferences[PreferencesKeys.BLACKLISTED_FOLDERS] = s.blacklistedFolders
            preferences[PreferencesKeys.WHITELISTED_FOLDERS] = s.whitelistedFolders
            preferences[PreferencesKeys.USE_WHITELIST_ONLY] = s.useWhitelistOnly
            preferences[PreferencesKeys.GAPLESS_PLAYBACK] = s.gaplessPlayback
            preferences[PreferencesKeys.CROSSFADE_ENABLED] = s.crossfadeEnabled
            preferences[PreferencesKeys.CROSSFADE_DURATION_SEC] = s.crossfadeDurationSec
            preferences[PreferencesKeys.ALWAYS_SHUFFLE] = s.alwaysShuffle
            preferences[PreferencesKeys.SHOW_AUDIO_DETAILS] = s.showAudioDetails
            preferences[PreferencesKeys.LOCK_SCREEN_PLAYER] = s.lockScreenPlayer
            preferences[PreferencesKeys.MUSIC_DYNAMIC_THEMING] = s.musicDynamicTheming
            preferences[PreferencesKeys.CHANGE_FONTS_WITH_MUSIC] = s.changeFontsWithMusic
            preferences[PreferencesKeys.DEFAULT_FONT_FAMILY] = s.defaultFontFamily
            preferences[PreferencesKeys.BACKDROP_BLUR_ENABLED] = s.backdropBlurEnabled
            preferences[PreferencesKeys.GLASSMORPHIC_EFFECT_ENABLED] = s.glassmorphicEffectEnabled
            preferences[PreferencesKeys.SNOWBALL_EFFECT_ENABLED] = s.snowballEffectEnabled
            preferences[PreferencesKeys.LIVE_MOVING_GRADIENT_ENABLED] = s.liveMovingGradientEnabled
            preferences[PreferencesKeys.HOME_SCREEN_EFFECTS_ENABLED] = s.homeScreenEffectsEnabled
            preferences[PreferencesKeys.HOME_SCREEN_EFFECT_STYLE] = s.homeScreenEffectStyle
            preferences[PreferencesKeys.PLAYBACK_VISUALIZER_ENABLED] = s.playbackVisualizerEnabled
            preferences[PreferencesKeys.WAVEFORM_VISUALIZER_STYLE] = s.waveformVisualizerStyle
            preferences[PreferencesKeys.NOTIFICATION_PATTERN] = s.notificationPattern.name
            preferences[PreferencesKeys.SILENT_NOTIFICATION] = s.silentNotification
            preferences[PreferencesKeys.LIBRARY_SORT_METHOD] = s.librarySortMethod
            preferences[PreferencesKeys.LIBRARY_SORT_ASCENDING] = s.librarySortAscending
            preferences[PreferencesKeys.LIBRARY_TAB] = s.libraryTab
            preferences[PreferencesKeys.AOD_ENABLED] = s.aodEnabled
            preferences[PreferencesKeys.AOD_LAYOUT_TYPE] = s.aodLayoutType.name
            preferences[PreferencesKeys.AOD_KEEP_SCREEN_AWAKE] = s.aodKeepScreenAwake
            preferences[PreferencesKeys.AOD_BURN_IN_PROTECTION] = s.aodBurnInProtection
            preferences[PreferencesKeys.AOD_BRIGHTNESS_DIM] = s.aodBrightnessDim

            // M3
            preferences[PreferencesKeys.M3_PLAYER_STYLE] = s.m3ExpressiveConfig.playerStyle.name
            preferences[PreferencesKeys.M3_ARTWORK_SHAPE] = s.m3ExpressiveConfig.artworkShape.name
            preferences[PreferencesKeys.M3_ARTWORK_SIZE] = s.m3ExpressiveConfig.artworkSize.name
            preferences[PreferencesKeys.M3_SCRUBBER_STYLE] = s.m3ExpressiveConfig.scrubberStyle.name
            preferences[PreferencesKeys.M3_HERO_BUTTON_STYLE] = s.m3ExpressiveConfig.heroButtonStyle.name
            preferences[PreferencesKeys.M3_CONTROL_BUTTON_STYLE] = s.m3ExpressiveConfig.controlButtonStyle.name
            preferences[PreferencesKeys.M3_ELEMENT_PLACEMENT] = s.m3ExpressiveConfig.elementPlacement.name
            preferences[PreferencesKeys.M3_INFO_ALIGNMENT] = s.m3ExpressiveConfig.infoAlignment.name
            preferences[PreferencesKeys.M3_BACKGROUND_STYLE] = s.m3ExpressiveConfig.backgroundStyle.name
            preferences[PreferencesKeys.M3_SHOW_VISUALIZER] = s.m3ExpressiveConfig.showVisualizer
            preferences[PreferencesKeys.M3_SHOW_AUDIO_SPECS] = s.m3ExpressiveConfig.showAudioSpecs
            preferences[PreferencesKeys.M3_SHOW_VOLUME_SLIDER] = s.m3ExpressiveConfig.showVolumeSlider
            preferences[PreferencesKeys.M3_SHOW_DEVICE_CAPSULE] = s.m3ExpressiveConfig.showDeviceCapsule
            preferences[PreferencesKeys.M3_SHOW_ACTION_PILLS] = s.m3ExpressiveConfig.showActionPills
            preferences[PreferencesKeys.M3_SHOW_SHUFFLE_REPEAT] = s.m3ExpressiveConfig.showShuffleRepeat
            preferences[PreferencesKeys.M3_SHOW_FAVORITE_BUTTON] = s.m3ExpressiveConfig.showFavoriteButton
            preferences[PreferencesKeys.M3_SHOW_TRACK_DETAILS] = s.m3ExpressiveConfig.showTrackDetails
            preferences[PreferencesKeys.M3_SHOW_LYRICS_CARD] = s.m3ExpressiveConfig.showLyricsCard
            preferences[PreferencesKeys.M3_EXPRESSIVE_HAPTICS] = s.m3ExpressiveConfig.expressiveHaptics
            preferences[PreferencesKeys.M3_BUTTON_GLOW] = s.m3ExpressiveConfig.buttonGlowEffect
            preferences[PreferencesKeys.M3_SHOW_VINYL_TONEARM] = s.m3ExpressiveConfig.showVinylTonearm
            preferences[PreferencesKeys.M3_SHOW_VU_METERS] = s.m3ExpressiveConfig.showVuMeters

            // Lyrics
            preferences[PreferencesKeys.LYRICS_TEXT_SIZE] = s.lyricsConfig.textSize.name
            preferences[PreferencesKeys.LYRICS_TEXT_ALIGNMENT] = s.lyricsConfig.textAlignment.name
            preferences[PreferencesKeys.LYRICS_INACTIVE_CONTRAST] = s.lyricsConfig.inactiveTextContrast
            preferences[PreferencesKeys.LYRICS_BACKDROP_STYLE] = s.lyricsConfig.backdropStyle.name
            preferences[PreferencesKeys.LYRICS_BLUR_INTENSITY] = s.lyricsConfig.blurIntensity
            preferences[PreferencesKeys.LYRICS_KARAOKE_FILL] = s.lyricsConfig.karaokeWordFillEnabled
            preferences[PreferencesKeys.LYRICS_SHOW_MINI_TRANSPORT] = s.lyricsConfig.showMiniTransportDeck
            preferences[PreferencesKeys.LYRICS_AUTO_SCROLL] = s.lyricsConfig.autoScrollEnabled
            preferences[PreferencesKeys.LYRICS_TAP_TO_SEEK] = s.lyricsConfig.tapToSeekEnabled
            preferences[PreferencesKeys.LYRICS_SHOW_AUDIO_BADGE] = s.lyricsConfig.showAudioQualityBadge
            preferences[PreferencesKeys.LYRICS_GLOW_BLOOM] = s.lyricsConfig.lyricGlowBloom
            preferences[PreferencesKeys.LYRICS_STAGGERED_SLIDE_IN] = s.lyricsConfig.staggeredSlideIn
        }
        persistFastCache(s)
    }
}
