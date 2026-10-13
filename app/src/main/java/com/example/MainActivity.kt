package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AudioDeviceType
import com.example.model.LyricLine
import com.example.model.LyricsUiState
import com.example.model.Track
import com.example.ui.components.SleepTimerBottomSheet
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.asPaddingValues
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowManager
import com.example.ui.aod.AodMusicScreen
import com.example.ui.artist.ArtistDetailScreen
import com.example.ui.artist.ArtistScreen
import com.example.ui.cast.ChromecastPickerSheet
import com.example.ui.components.SnowballEffectCanvas
import com.example.ui.components.AppSplashScreen
import com.example.ui.components.FloatingMiniPlayer
import com.example.ui.components.MainNavTab
import com.example.ui.components.MainScaffold
import com.example.ui.details.TrackDetailsScreen
import com.example.ui.equalizer.EqualizerScreen
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.lyrics.SyncedLyricsScreen
import com.example.ui.music.AllMusicScreen
import com.example.ui.player.NowPlayingScreen
import com.example.ui.player.PlayerViewModel
import com.example.ui.queue.QueueScreen
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyAppTheme
import com.example.ui.tracker.MusicTrackerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        enableEdgeToEdge()
        applyImmersiveFullScreen()
        setContent {
            val viewModel: PlayerViewModel = viewModel()
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()
            val playerUiState by viewModel.playerUiState.collectAsStateWithLifecycle()

            val isMusicActive = playerUiState.isPlaying && playerUiState.currentTrack.id.isNotBlank()
            val primaryAccent = if (isMusicActive) playerUiState.currentTrack.primaryAmbientColor else null
            val secondaryAccent = if (isMusicActive) playerUiState.currentTrack.secondaryAmbientColor else null

            MyAppTheme(
                themeMode = settings.themeMode,
                colorPalette = settings.colorPalette,
                useDynamicColors = settings.useDynamicColors,
                playingMusicAccent = primaryAccent,
                secondaryMusicAccent = secondaryAccent,
                musicThemingEnabled = settings.musicDynamicTheming && isMusicActive,
                changeFontsWithMusic = settings.changeFontsWithMusic,
                defaultFontFamily = settings.defaultFontFamily,
                currentTrack = playerUiState.currentTrack.takeIf { it.id.isNotBlank() }
            ) {
                val isAmoled = settings.themeMode == com.example.model.AppThemeMode.PURE_BLACK || settings.amoledBlack

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isAmoled) Color(0xFF000000) else MaterialTheme.colorScheme.background
                ) {
                    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
                    val topCameraMargin = if (cutoutTop > 28.dp) cutoutTop + 4.dp else 28.dp

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topCameraMargin)
                    ) {
                        MainScreen(
                            viewModel = viewModel,
                            isAmoledBlack = isAmoled
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveFullScreen()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveFullScreen()
        }
    }

    private fun applyImmersiveFullScreen() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }
}

/**
 * Configure Lock Screen Display behavior so music player shows as the main screen when phone locks.
 */
fun updateLockScreenVisibility(activity: ComponentActivity?, showWhenLocked: Boolean, isPlaying: Boolean) {
    if (activity == null) return
    val shouldShow = showWhenLocked && isPlaying
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        activity.setShowWhenLocked(shouldShow)
        activity.setTurnScreenOn(shouldShow)
    } else {
        @Suppress("DEPRECATION")
        if (shouldShow) {
            activity.window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        } else {
            activity.window.clearFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

/**
 * Configure peak refresh rate (90Hz / 120Hz) on supported displays
 */
@Suppress("DEPRECATION")
fun applyHighRefreshRate(window: android.view.Window, enable: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (enable) {
            val display = window.context.display
            val modes = display?.supportedModes ?: emptyArray()
            val maxMode = modes.maxByOrNull { it.refreshRate }
            val params = window.attributes
            if (maxMode != null) {
                params.preferredDisplayModeId = maxMode.modeId
            }
            window.attributes = params
        } else {
            val params = window.attributes
            params.preferredDisplayModeId = 0
            window.attributes = params
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val windowManager = window.context.getSystemService(android.content.Context.WINDOW_SERVICE) as? android.view.WindowManager
        val display = windowManager?.defaultDisplay
        val modes = display?.supportedModes ?: emptyArray()
        val maxMode = if (enable) modes.maxByOrNull { it.refreshRate } else null
        val params = window.attributes
        params.preferredDisplayModeId = maxMode?.modeId ?: 0
        window.attributes = params
    }
}

@Composable
fun MainScreen(
    viewModel: PlayerViewModel = viewModel(),
    isAmoledBlack: Boolean = false
) {
    PixelPlayerApp(
        viewModel = viewModel,
        isAmoledBlack = isAmoledBlack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelPlayerApp(
    viewModel: PlayerViewModel = viewModel(),
    isAmoledBlack: Boolean = false
) {
    val context = LocalContext.current
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val playerUiState by viewModel.playerUiState.collectAsStateWithLifecycle()
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val availableDevices by viewModel.availableDevices.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val trackerSummary by viewModel.trackerSummary.collectAsStateWithLifecycle()
    val favoriteTrackIds by viewModel.favoriteTrackIds.collectAsStateWithLifecycle()
    val chromecastUiState by viewModel.chromecastUiState.collectAsStateWithLifecycle()

    val activity = context as? ComponentActivity
    LaunchedEffect(settingsState.forceHighRefreshRate) {
        activity?.window?.let { win ->
            applyHighRefreshRate(win, settingsState.forceHighRefreshRate)
        }
    }

    var showDevicePickerSheet by remember { mutableStateOf(false) }
    var showChromecastPickerSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showLyricsEditorSheet by remember { mutableStateOf(false) }
    var lyricsEditorTrack by remember { mutableStateOf<Track?>(null) }
    var activeDestination by remember { mutableStateOf(ScreenDestination.Home) }
    var lastBrowsingDestination by remember { mutableStateOf(ScreenDestination.Home) }
    var inspectedTrack by remember { mutableStateOf<Track?>(null) }
    val homeListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(settingsState.lockScreenPlayer, playerUiState.isPlaying) {
        updateLockScreenVisibility(activity, settingsState.lockScreenPlayer, playerUiState.isPlaying)
    }

    LaunchedEffect(activeDestination, settingsState.aodKeepScreenAwake) {
        activity?.window?.let { win ->
            if (activeDestination == ScreenDestination.Aod && settingsState.aodKeepScreenAwake) {
                win.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Sync activeDestination with playerUiState
    LaunchedEffect(playerUiState.isNowPlayingExpanded) {
        if (playerUiState.isNowPlayingExpanded) {
            if (activeDestination != ScreenDestination.NowPlaying) {
                activeDestination = ScreenDestination.NowPlaying
            }
        } else if (activeDestination == ScreenDestination.NowPlaying || activeDestination == ScreenDestination.Lyrics) {
            activeDestination = lastBrowsingDestination
        }
    }

    // Permissions to request: Media Audio, Notifications, Nearby Devices (Bluetooth), and Microphone
    val permissionsToRequest = remember {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            list.add(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            list.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        list.add(android.Manifest.permission.RECORD_AUDIO)
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[android.Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            results[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        val micGranted = results[android.Manifest.permission.RECORD_AUDIO] == true
        val nearbyGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            results[android.Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            true
        }
        viewModel.setStoragePermission(storageGranted)
        viewModel.setMicrophonePermission(micGranted)
        viewModel.setNearbyDevicesPermission(nearbyGranted)
    }

    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setMicrophonePermission(granted)
    }

    val nearbyDevicesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNearbyDevicesPermission(granted)
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setStoragePermission(granted)
    }

    // Check permission state on initialization
    LaunchedEffect(Unit) {
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        val micGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val nearbyGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        viewModel.setStoragePermission(storageGranted)
        viewModel.setMicrophonePermission(micGranted)
        viewModel.setNearbyDevicesPermission(nearbyGranted)

        val ungranted = permissionsToRequest.filter { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        }
    }

    // Handle system back navigation
    BackHandler(enabled = activeDestination != ScreenDestination.Home) {
        when (activeDestination) {
            ScreenDestination.Search -> activeDestination = ScreenDestination.Home
            ScreenDestination.Artists -> activeDestination = ScreenDestination.Home
            ScreenDestination.Settings -> {
                if (playerUiState.isNowPlayingExpanded) {
                    activeDestination = ScreenDestination.NowPlaying
                } else {
                    activeDestination = lastBrowsingDestination
                }
            }
            ScreenDestination.AllMusic -> activeDestination = lastBrowsingDestination
            ScreenDestination.ArtistDetail -> activeDestination = ScreenDestination.Artists
            ScreenDestination.Queue -> {
                if (playerUiState.isNowPlayingExpanded) {
                    activeDestination = ScreenDestination.NowPlaying
                } else {
                    activeDestination = lastBrowsingDestination
                }
            }
            ScreenDestination.Equalizer -> {
                if (playerUiState.isNowPlayingExpanded) {
                    activeDestination = ScreenDestination.NowPlaying
                } else {
                    activeDestination = lastBrowsingDestination
                }
            }
            ScreenDestination.MusicTracker -> activeDestination = lastBrowsingDestination
            ScreenDestination.TrackDetails -> {
                if (playerUiState.isNowPlayingExpanded) {
                    activeDestination = ScreenDestination.NowPlaying
                } else {
                    activeDestination = lastBrowsingDestination
                }
            }
            ScreenDestination.Lyrics -> {
                viewModel.setLyricsVisible(false)
                activeDestination = ScreenDestination.NowPlaying
            }
            ScreenDestination.NowPlaying -> {
                viewModel.setNowPlayingExpanded(false)
                activeDestination = lastBrowsingDestination
            }
            ScreenDestination.Aod -> {
                if (playerUiState.isNowPlayingExpanded) {
                    activeDestination = ScreenDestination.NowPlaying
                } else {
                    activeDestination = lastBrowsingDestination
                }
            }
            ScreenDestination.Home -> Unit
        }
    }

    val screenBackgroundColor = if (isAmoledBlack) Color.Black else Color(0xFF090D14)

    // Browsing screens that show the stacked floating bottom bar
    val isBrowsingScreen = activeDestination == ScreenDestination.Home ||
            activeDestination == ScreenDestination.Search ||
            activeDestination == ScreenDestination.Artists

    // Map active destination to MainNavTab for M3 bottom navigation item indicator
    val currentNavTab = when (activeDestination) {
        ScreenDestination.Search -> MainNavTab.SEARCH
        ScreenDestination.Artists -> MainNavTab.ARTISTS
        else -> MainNavTab.HOME
    }

    MainScaffold(
        currentTab = currentNavTab,
        onTabSelected = { tab ->
            val dest = when (tab) {
                MainNavTab.HOME -> ScreenDestination.Home
                MainNavTab.SEARCH -> ScreenDestination.Search
                MainNavTab.ARTISTS -> ScreenDestination.Artists
            }
            lastBrowsingDestination = dest
            activeDestination = dest
        },
        playerUiState = playerUiState,
        onMiniPlayerClick = { viewModel.setNowPlayingExpanded(true) },
        onPlayPauseClick = { viewModel.togglePlayPause() },
        onSkipNextClick = { viewModel.skipNext() },
        onSkipPreviousClick = { viewModel.skipPrevious() },
        showBottomBars = isBrowsingScreen,
        isAmoledBlack = isAmoledBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundColor)
        ) {
            // Navigation Screen Transition with Motion Physics Springs
            AnimatedContent(
                targetState = activeDestination,
                transitionSpec = {
                    val isInitialBrowsing = initialState == ScreenDestination.Home ||
                            initialState == ScreenDestination.Search ||
                            initialState == ScreenDestination.Artists

                    val isTargetBrowsing = targetState == ScreenDestination.Home ||
                            targetState == ScreenDestination.Search ||
                            targetState == ScreenDestination.Artists

                    when {
                        // Smooth tab-to-tab switching with spring physics
                        isInitialBrowsing && isTargetBrowsing -> {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.96f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleOut(targetScale = 1.02f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                        }
                        targetState == ScreenDestination.Lyrics -> {
                            (slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 3 } + fadeOut())
                        }
                        initialState == ScreenDestination.Lyrics -> {
                            (slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn())
                                .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeOut())
                        }
                        targetState == ScreenDestination.NowPlaying -> {
                            (slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it } +
                                scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleOut(targetScale = 0.96f))
                        }
                        initialState == ScreenDestination.NowPlaying -> {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) { it } + fadeOut())
                        }
                        targetState == ScreenDestination.Settings || targetState == ScreenDestination.Equalizer || targetState == ScreenDestination.Queue || targetState == ScreenDestination.MusicTracker -> {
                            (slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                scaleIn(initialScale = 0.95f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)) +
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleOut(targetScale = 0.98f))
                        }
                        else -> {
                            (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.97f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                        }
                    }
                },
                label = "screenNavigationTransition"
            ) { destination ->
                when (destination) {
                    ScreenDestination.Home -> {
                        HomeScreen(
                            homeUiState = homeUiState,
                            currentTrack = playerUiState.currentTrack,
                            isPlaying = playerUiState.isPlaying,
                            isFavorite = playerUiState.isFavorite,
                            favoriteTrackIds = favoriteTrackIds,
                            trackerSummary = trackerSummary,
                            initialSortMethod = settingsState.librarySortMethod,
                            initialSortAscending = settingsState.librarySortAscending,
                            onSortChange = { method, asc -> viewModel.setLibrarySort(method, asc) },
                            onOpenTracker = { activeDestination = ScreenDestination.MusicTracker },
                            onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onNextClick = { viewModel.skipNext() },
                            onShuffleAllClick = { viewModel.shuffleAll() },
                            onToggleFavoriteTrack = { trackId -> viewModel.toggleFavoriteTrack(trackId) },
                            onDeleteTrack = { track -> viewModel.deleteTrack(track) },
                            onEditMetadata = { trackId, title, artist, album -> viewModel.updateTrackMetadata(trackId, title, artist, album) },
                            onPlayNextClick = { track -> viewModel.playNext(track) },
                            onAddToQueueClick = { track -> viewModel.addToQueue(track) },
                            onEditLyricsClick = { track ->
                                inspectedTrack = track
                                lyricsEditorTrack = track
                                showLyricsEditorSheet = true
                            },
                            onSettingsClick = { activeDestination = ScreenDestination.Settings },
                            onOpenChromecast = { showChromecastPickerSheet = true },
                            isChromecastConnected = chromecastUiState.activeDevice != null,
                            chromecastDeviceName = chromecastUiState.activeDevice?.name,
                            onRequestPermission = { permissionLauncher.launch(permissionsToRequest) },
                            onRequestMicrophonePermission = { microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                            onRequestNearbyDevicesPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    nearbyDevicesLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                                } else {
                                    viewModel.setNearbyDevicesPermission(true)
                                }
                            },
                            onRescanClick = { viewModel.scanLocalStorage() },
                            onTrackDetailsClick = { track ->
                                inspectedTrack = track
                                activeDestination = ScreenDestination.TrackDetails
                            },
                            contentPadding = innerPadding,
                            listState = homeListState,
                            isAmoledBlack = isAmoledBlack,
                            backdropBlurEnabled = settingsState.backdropBlurEnabled,
                            liveMovingGradientEnabled = settingsState.liveMovingGradientEnabled,
                            homeScreenEffectsEnabled = settingsState.homeScreenEffectsEnabled,
                            homeScreenEffectStyle = settingsState.homeScreenEffectStyle,
                            blurIntensity = settingsState.blurIntensity,
                            playbackVisualizerEnabled = settingsState.playbackVisualizerEnabled,
                            waveformVisualizerStyle = settingsState.waveformVisualizerStyle
                        )
                    }

                    ScreenDestination.Search -> {
                        SearchScreen(
                            homeUiState = homeUiState,
                            playerUiState = playerUiState,
                            onSearchQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onTrackDetailsClick = { track ->
                                inspectedTrack = track
                                activeDestination = ScreenDestination.TrackDetails
                            },
                            contentPadding = innerPadding,
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.NowPlaying -> {
                        NowPlayingScreen(
                            uiState = playerUiState,
                            layoutType = settingsState.playerLayoutType,
                            lyricsUiState = lyricsUiState,
                            showAudioDetails = settingsState.showAudioDetails,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onNextClick = { viewModel.skipNext() },
                            onSeekTo = { posMs -> viewModel.seekTo(posMs) },
                            onFavoriteToggle = { viewModel.toggleFavorite() },
                            onVolumeChange = { vol -> viewModel.setVolume(vol) },
                            onLyricsToggle = { viewModel.toggleLyricsScreen() },
                            onCollapseClick = { viewModel.setNowPlayingExpanded(false) },
                            onDevicePickerClick = { showDevicePickerSheet = true },
                            onEqualizerClick = { activeDestination = ScreenDestination.Equalizer },
                            onQueueClick = { activeDestination = ScreenDestination.Queue },
                            onShuffleToggle = { viewModel.toggleShuffle() },
                            onRepeatToggle = { viewModel.toggleRepeat() },
                            onSleepTimerClick = { showSleepTimerSheet = true },
                            onPlaybackSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) },
                            onOpenSettings = { activeDestination = ScreenDestination.Settings },
                            onTrackDetailsClick = {
                                inspectedTrack = playerUiState.currentTrack
                                activeDestination = ScreenDestination.TrackDetails
                            },
                            onEditLyricsClick = {
                                lyricsEditorTrack = playerUiState.currentTrack
                                showLyricsEditorSheet = true
                            },
                            blurIntensity = settingsState.blurIntensity,
                            isAmoledBlack = settingsState.amoledBlack || settingsState.themeMode == com.example.model.AppThemeMode.PURE_BLACK,
                            isGlassmorphicEffectEnabled = settingsState.glassmorphicEffectEnabled,
                            snowballEffectEnabled = settingsState.snowballEffectEnabled,
                            liveMovingGradientEnabled = settingsState.liveMovingGradientEnabled,
                            showMusicVisualizer = settingsState.playbackVisualizerEnabled,
                            waveformVisualizerStyle = settingsState.waveformVisualizerStyle,
                            onWaveformVisualizerStyleChange = { viewModel.setWaveformVisualizerStyle(it) },
                            m3Config = settingsState.m3ExpressiveConfig,
                            onM3ConfigChange = { viewModel.updateM3ExpressiveConfig(it) },
                            lyricsConfig = settingsState.lyricsConfig,
                            changeFontsWithMusic = settingsState.changeFontsWithMusic,
                            defaultFontFamily = settingsState.defaultFontFamily
                        )
                    }

                    ScreenDestination.Lyrics -> {
                        SyncedLyricsScreen(
                            uiState = lyricsUiState,
                            currentTrack = playerUiState.currentTrack,
                            onLyricClick = { timestampMs -> viewModel.seekTo(timestampMs) },
                            onBackClick = { viewModel.setLyricsVisible(false) },
                            onFavoriteToggle = { viewModel.toggleFavorite() },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onNextClick = { viewModel.skipNext() },
                            onEditLyricsClick = {
                                lyricsEditorTrack = playerUiState.currentTrack
                                showLyricsEditorSheet = true
                            },
                            config = settingsState.lyricsConfig,
                            blurIntensity = settingsState.lyricsConfig.blurIntensity,
                            isAmoledBlack = settingsState.lyricsConfig.backdropStyle == com.example.model.LyricsBackdropStyle.PURE_AMOLED_BLACK || isAmoledBlack
                        )
                    }

                    ScreenDestination.Equalizer -> {
                        val equalizerUiState by viewModel.equalizerUiState.collectAsStateWithLifecycle()
                        EqualizerScreen(
                            uiState = equalizerUiState,
                            onToggleEqualizer = { enabled -> viewModel.toggleEqualizer(enabled) },
                            onBandGainChange = { bandIndex, gainMb -> viewModel.setEqBandGain(bandIndex, gainMb) },
                            onPresetSelect = { presetIdx -> viewModel.setEqPreset(presetIdx) },
                            onBassBoostChange = { strength -> viewModel.setEqBassBoost(strength) },
                            onVirtualizerChange = { strength -> viewModel.setEqVirtualizer(strength) },
                            onReverbPresetChange = { preset -> viewModel.setEqReverb(preset) },
                            onLoudnessGainChange = { gainMb -> viewModel.setEqLoudness(gainMb) },
                            onResetToFlat = { viewModel.resetEqualizerToFlat() },
                            onOpenSystemEqualizer = { viewModel.openSystemEqualizer() },
                            onBackClick = {
                                if (playerUiState.isNowPlayingExpanded) {
                                    activeDestination = ScreenDestination.NowPlaying
                                } else {
                                    activeDestination = ScreenDestination.Home
                                }
                            },
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.Settings -> {
                        val isCachingImages by viewModel.isCachingImages.collectAsStateWithLifecycle()
                        val cachingProgress by viewModel.cachingProgress.collectAsStateWithLifecycle()
                        val localSnapshotSummary by viewModel.localSnapshotInfo.collectAsStateWithLifecycle()
                        val backupStatusMessage by viewModel.backupStatus.collectAsStateWithLifecycle()
                        val audioEngineStatus by viewModel.audioEngineStatus.collectAsStateWithLifecycle()

                        SettingsScreen(
                            settings = settingsState,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            onColorPaletteChange = { viewModel.setColorPalette(it) },
                            onUseDynamicColorsChange = { viewModel.setUseDynamicColors(it) },
                            onDarkThemeChange = { viewModel.setDarkTheme(it) },
                            onAmoledBlackChange = { viewModel.setAmoledBlack(it) },
                            onDynamicColorChange = { viewModel.setDynamicColor(it) },
                            onBlurIntensityChange = { viewModel.setBlurIntensity(it) },
                            onPlayerLayoutTypeChange = { viewModel.setPlayerLayoutType(it) },
                            onM3ExpressiveConfigChange = { viewModel.updateM3ExpressiveConfig(it) },
                            onLyricsConfigChange = { viewModel.updateLyricsConfig(it) },
                            onNotificationPatternChange = { viewModel.setNotificationPattern(it) },
                            onSilentNotificationChange = { viewModel.setSilentNotification(it) },
                            onForceHighRefreshRateChange = { viewModel.setForceHighRefreshRate(it) },
                            onMinDurationChange = { viewModel.setMinDurationSec(it) },
                            onAddBlacklistFolder = { viewModel.addBlacklistFolder(it) },
                            onRemoveBlacklistFolder = { viewModel.removeBlacklistFolder(it) },
                            onAddWhitelistFolder = { viewModel.addWhitelistFolder(it) },
                            onRemoveWhitelistFolder = { viewModel.removeWhitelistFolder(it) },
                            onUseWhitelistOnlyChange = { viewModel.setUseWhitelistOnly(it) },
                            onGaplessPlaybackChange = { viewModel.setGaplessPlayback(it) },
                            onCrossfadeEnabledChange = { viewModel.setCrossfadeEnabled(it) },
                            onCrossfadeDurationSecChange = { viewModel.setCrossfadeDurationSec(it) },
                            onAlwaysShuffleChange = { viewModel.setAlwaysShuffle(it) },
                            onShowAudioDetailsChange = { viewModel.setShowAudioDetails(it) },
                            onLockScreenPlayerChange = { viewModel.setLockScreenPlayer(it) },
                            onMusicDynamicThemingChange = { viewModel.setMusicDynamicTheming(it) },
                            onChangeFontsWithMusicChange = { viewModel.setChangeFontsWithMusic(it) },
                            onDefaultFontFamilyChange = { viewModel.setDefaultFontFamily(it) },
                            onBackdropBlurEnabledChange = { viewModel.setBackdropBlurEnabled(it) },
                            onGlassmorphicEffectEnabledChange = { viewModel.setGlassmorphicEffectEnabled(it) },
                            onSnowballEffectEnabledChange = { viewModel.setSnowballEffectEnabled(it) },
                            onLiveMovingGradientChange = { viewModel.setLiveMovingGradientEnabled(it) },
                            onHomeScreenEffectsChange = { viewModel.setHomeScreenEffectsEnabled(it) },
                            onHomeScreenEffectStyleChange = { viewModel.setHomeScreenEffectStyle(it) },
                            onPlaybackVisualizerEnabledChange = { viewModel.setPlaybackVisualizerEnabled(it) },
                            onWaveformVisualizerStyleChange = { viewModel.setWaveformVisualizerStyle(it) },
                            onAodEnabledChange = { viewModel.setAodEnabled(it) },
                            onAodLayoutTypeChange = { viewModel.setAodLayoutType(it) },
                            onAodKeepScreenAwakeChange = { viewModel.setAodKeepScreenAwake(it) },
                            onAodBurnInProtectionChange = { viewModel.setAodBurnInProtection(it) },
                            onAodBrightnessDimChange = { viewModel.setAodBrightnessDim(it) },
                            onPreviewAodClick = { activeDestination = ScreenDestination.Aod },
                            onCacheImagesClick = { viewModel.cacheAllImages() },
                            isCachingImages = isCachingImages,
                            cachingProgress = cachingProgress,
                            onOpenSystemEqualizer = { viewModel.openSystemEqualizer() },
                            onOpenBuiltInEqualizer = { activeDestination = ScreenDestination.Equalizer },
                            onUseSystemEqualizerChange = { viewModel.setUseSystemEqualizerPref(it) },
                            onRescanClick = { viewModel.scanLocalStorage() },
                            sleepTimerState = sleepTimerState,
                            onOpenSleepTimer = { showSleepTimerSheet = true },
                            chromecastUiState = chromecastUiState,
                            onOpenChromecast = { showChromecastPickerSheet = true },
                            onDisconnectChromecast = { viewModel.disconnectCast() },
                            onSetCastQuality = { viewModel.setCastQuality(it) },
                            onScanCastDevices = { viewModel.startCastDiscovery() },
                            audioEngineStatus = audioEngineStatus,
                            onAudioRoutingModeChange = { viewModel.setAudioRoutingMode(it) },
                            hasStoragePermission = homeUiState.hasStoragePermission,
                            hasMicrophonePermission = homeUiState.hasMicrophonePermission,
                            hasNearbyDevicesPermission = homeUiState.hasNearbyDevicesPermission,
                            onRequestStoragePermission = {
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    android.Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                storageLauncher.launch(perm)
                            },
                            onRequestMicrophonePermission = {
                                microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            },
                            onRequestNearbyDevicesPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    nearbyDevicesLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
                                } else {
                                    viewModel.setNearbyDevicesPermission(true)
                                }
                            },
                            onOpenTracker = { activeDestination = ScreenDestination.MusicTracker },
                            localSnapshotSummary = localSnapshotSummary,
                            backupStatusMessage = backupStatusMessage,
                            onClearBackupStatus = { viewModel.clearBackupStatus() },
                            onExportBackupToUri = { uri -> viewModel.exportBackupToUri(uri) },
                            onRestoreBackupFromUri = { uri, merge -> viewModel.restoreBackupFromUri(uri, merge) },
                            onCreateLocalSnapshot = { viewModel.createLocalSnapshot() },
                            onRestoreLocalSnapshot = { merge -> viewModel.restoreLocalSnapshot(merge) },
                            onLoadSnapshotInfo = { viewModel.loadLocalSnapshotInfo() },
                            onBackClick = {
                                if (playerUiState.isNowPlayingExpanded) {
                                    activeDestination = ScreenDestination.NowPlaying
                                } else {
                                    activeDestination = ScreenDestination.Home
                                }
                            }
                        )
                    }

                    ScreenDestination.AllMusic -> {
                        AllMusicScreen(
                            tracks = homeUiState.tracks,
                            currentPlayingTrackId = playerUiState.currentTrack.id,
                            isPlaying = playerUiState.isPlaying,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onShuffleAllClick = { viewModel.shuffleAll() },
                            contentPadding = innerPadding,
                            showAudioDetails = settingsState.showAudioDetails
                        )
                    }

                    ScreenDestination.Artists -> {
                        val artists by viewModel.artists.collectAsStateWithLifecycle()
                        ArtistScreen(
                            artists = artists,
                            onArtistClick = { artist ->
                                viewModel.selectArtist(artist)
                                activeDestination = ScreenDestination.ArtistDetail
                            },
                            contentPadding = innerPadding,
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.ArtistDetail -> {
                        val currentArtist = selectedArtist
                        if (currentArtist != null) {
                            ArtistDetailScreen(
                                artist = currentArtist,
                                onBackClick = { activeDestination = ScreenDestination.Artists },
                                onTrackClick = { track -> viewModel.playTrack(track) },
                                onPlayAllClick = { tracks -> viewModel.playArtistTracks(tracks, shuffle = false) },
                                onShuffleClick = { tracks -> viewModel.playArtistTracks(tracks, shuffle = true) },
                                contentPadding = innerPadding,
                                isAmoledBlack = isAmoledBlack,
                                showAudioDetails = settingsState.showAudioDetails
                            )
                        } else {
                            activeDestination = ScreenDestination.Artists
                        }
                    }

                    ScreenDestination.Queue -> {
                        QueueScreen(
                            playlist = playerUiState.playlist,
                            currentTrack = playerUiState.currentTrack,
                            isPlaying = playerUiState.isPlaying,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onRemoveFromQueue = { index -> viewModel.removeFromQueue(index) },
                            onMoveTrack = { from, to -> viewModel.moveQueueItem(from, to) },
                            onShuffleQueue = { viewModel.shuffleQueue() },
                            onBackClick = {
                                if (playerUiState.isNowPlayingExpanded) {
                                    activeDestination = ScreenDestination.NowPlaying
                                } else {
                                    activeDestination = lastBrowsingDestination
                                }
                            },
                            contentPadding = innerPadding,
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.TrackDetails -> {
                        val trackToInspect = inspectedTrack ?: playerUiState.currentTrack
                        TrackDetailsScreen(
                            track = trackToInspect,
                            isPlaying = playerUiState.isPlaying && playerUiState.currentTrack.id == trackToInspect.id,
                            onPlayPauseClick = {
                                if (playerUiState.currentTrack.id == trackToInspect.id) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.playTrack(trackToInspect)
                                }
                            },
                            onFavoriteToggle = {
                                if (playerUiState.currentTrack.id == trackToInspect.id) {
                                    viewModel.toggleFavorite()
                                }
                            },
                            onOpenEqualizer = { activeDestination = ScreenDestination.Equalizer },
                            onOpenLyrics = {
                                lyricsEditorTrack = trackToInspect
                                showLyricsEditorSheet = true
                            },
                            onBackClick = {
                                if (playerUiState.isNowPlayingExpanded) {
                                    activeDestination = ScreenDestination.NowPlaying
                                } else {
                                    activeDestination = lastBrowsingDestination
                                }
                            },
                            contentPadding = innerPadding,
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.MusicTracker -> {
                        MusicTrackerScreen(
                            summary = trackerSummary,
                            currentPlayingTrackId = playerUiState.currentTrack.id,
                            isPlaying = playerUiState.isPlaying,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onPlayAllTopClick = { shuffle -> viewModel.playMostListened(shuffle) },
                            onClearStatsClick = { viewModel.clearTrackerStats() },
                            onBackClick = { activeDestination = ScreenDestination.Home },
                            contentPadding = innerPadding,
                            isAmoledBlack = isAmoledBlack
                        )
                    }

                    ScreenDestination.Aod -> {
                        AodMusicScreen(
                            uiState = playerUiState,
                            layoutType = settingsState.aodLayoutType,
                            burnInProtection = settingsState.aodBurnInProtection,
                            dimFactor = settingsState.aodBrightnessDim,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onNextClick = { viewModel.skipNext() },
                            onExitAod = {
                                if (playerUiState.isNowPlayingExpanded) {
                                    activeDestination = ScreenDestination.NowPlaying
                                } else {
                                    activeDestination = ScreenDestination.Home
                                }
                            }
                        )
                    }
                }
            }

            // Independent Global Snowball Particle Canvas Overlay
            if (settingsState.snowballEffectEnabled) {
                SnowballEffectCanvas(
                    particleCount = 55,
                    isPlaying = playerUiState.isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Active Output Device Picker Bottom Sheet
        if (showDevicePickerSheet) {
            val context = LocalContext.current
            ModalBottomSheet(
                onDismissRequest = { showDevicePickerSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF131B2A),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Audio Output",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select active stream destination",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF88A0BF)
                            )
                        }
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF38BDF8), CircleShape)
                                )
                                Text(
                                    text = playerUiState.activeDevice.name.take(16),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // System Default (Auto-Route) Option
                    val isAutoSelected = playerUiState.activeDevice.isSystemDefault ||
                        availableDevices.none { it.id != 0 && it.id == playerUiState.activeDevice.id } &&
                        availableDevices.none { it.name == playerUiState.activeDevice.name }

                    Surface(
                        onClick = {
                            viewModel.resetAudioRoutingToDefault()
                            showDevicePickerSheet = false
                        },
                        color = if (isAutoSelected) Color(0x2238BDF8) else Color(0xFF1A2333),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAutoSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF243044),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isAutoSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System Default (Auto-Route)",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Automatically switches to headphones/Bluetooth",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                            }
                            if (isAutoSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Quick Chromecast Output Tile in Device Picker
                    Surface(
                        onClick = {
                            showDevicePickerSheet = false
                            showChromecastPickerSheet = true
                        },
                        color = if (chromecastUiState.activeDevice != null) Color(0x3338BDF8) else Color(0xFF1E2A3B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (chromecastUiState.activeDevice != null) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0xFF28364D),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (chromecastUiState.activeDevice != null) Icons.Default.CastConnected else Icons.Default.Cast,
                                        contentDescription = null,
                                        tint = if (chromecastUiState.activeDevice != null) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (chromecastUiState.activeDevice != null) "Casting to ${chromecastUiState.activeDevice?.name}" else "Chromecast & Smart Speakers",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (chromecastUiState.activeDevice != null) "Connected via Wi-Fi • Tap to manage" else "Stream wirelessly to Nest, Google TV & speakers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            if (chromecastUiState.activeDevice != null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Connected Outputs (${availableDevices.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    availableDevices.forEach { device ->
                        val isSelected = !isAutoSelected && (
                            (device.id != 0 && device.id == playerUiState.activeDevice.id) ||
                                (device.name == playerUiState.activeDevice.name && device.type == playerUiState.activeDevice.type)
                        )
                        val icon = when (device.type) {
                            AudioDeviceType.BLUETOOTH -> Icons.Default.Bluetooth
                            AudioDeviceType.WIRED_HEADPHONES -> Icons.Default.Headphones
                            AudioDeviceType.USB_DAC -> Icons.Default.Headphones
                            AudioDeviceType.SPEAKER -> Icons.Default.Speaker
                            else -> Icons.Default.PhoneAndroid
                        }

                        Surface(
                            onClick = {
                                viewModel.selectDevice(device)
                                showDevicePickerSheet = false
                            },
                            color = if (isSelected) Color(0x2238BDF8) else Color(0xFF1A2333),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF243044),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = device.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.55f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick System Panel Launcher
                    Surface(
                        onClick = {
                            viewModel.openSystemAudioSettings(context)
                            showDevicePickerSheet = false
                        },
                        color = Color(0xFF222F43),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pair New Device / System Output Settings",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF7DD3FC)
                            )
                        }
                    }
                }
            }
        }

        // Up Next Playing Queue Bottom Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF19202E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Up Next Queue",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${playerUiState.playlist.size} tracks in playlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(playerUiState.playlist) { index, track ->
                            val isCurrent = track.id == playerUiState.currentTrack.id
                            Surface(
                                onClick = {
                                    viewModel.playTrack(track)
                                    showQueueSheet = false
                                },
                                color = if (isCurrent) Color(0x33446699) else Color(0x14FFFFFF),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF242C3D)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = if (isCurrent) Color(0xFF90BAFF) else Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${track.artist} • ${track.album}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.6f),
                                            maxLines = 1
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Currently Playing",
                                            tint = Color(0xFF90BAFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sleep Timer Bottom Sheet
        if (showSleepTimerSheet) {
            SleepTimerBottomSheet(
                sleepTimerState = sleepTimerState,
                onStartTimer = { minutes -> viewModel.startSleepTimer(minutes) },
                onStartEndOfTrackTimer = { viewModel.startSleepTimerEndOfTrack() },
                onAddMinutes = { minutes -> viewModel.addSleepTimerMinutes(minutes) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerSheet = false },
                isAmoledBlack = isAmoledBlack
            )
        }

        // Interactive Material 3 Expressive Lyrics Studio Editor
        if (showLyricsEditorSheet) {
            val targetTrack = lyricsEditorTrack ?: playerUiState.currentTrack
            val currentLines = (lyricsUiState as? LyricsUiState.Success)?.lines ?: emptyList()
            val currentPlain = (lyricsUiState as? LyricsUiState.Success)?.plainLyrics
            val isSynced = (lyricsUiState as? LyricsUiState.Success)?.isSynced ?: (currentLines.isNotEmpty())

            com.example.ui.lyrics.LyricsEditorSheet(
                track = targetTrack,
                initialLines = currentLines,
                initialPlainText = currentPlain,
                initialIsSynced = isSynced,
                currentPlaybackPositionMs = playerUiState.currentPositionMs,
                onSeekTo = { posMs -> viewModel.seekTo(posMs) },
                onSaveLyrics = { lines, plainText, synced ->
                    viewModel.saveCustomLyrics(targetTrack.id, lines, plainText, synced)
                    showLyricsEditorSheet = false
                },
                onFetchOnline = { artist, title ->
                    viewModel.searchOnlineLyrics(artist, title)
                },
                onDismiss = { showLyricsEditorSheet = false }
            )
        }

        // Chromecast & Wireless Audio Output Picker Bottom Sheet
        if (showChromecastPickerSheet) {
            ChromecastPickerSheet(
                uiState = chromecastUiState,
                currentTrack = playerUiState.currentTrack,
                onDismiss = { showChromecastPickerSheet = false },
                onStartScan = { viewModel.startCastDiscovery() },
                onConnectDevice = { device -> viewModel.connectToCastDevice(device) },
                onDisconnect = { viewModel.disconnectCast() },
                onVolumeChange = { vol -> viewModel.setCastVolume(vol) },
                onQualityChange = { quality -> viewModel.setCastQuality(quality) },
                isAmoledBlack = isAmoledBlack
            )
        }
    }
}

private enum class ScreenDestination {
    Home,
    Search,
    Queue,
    AllMusic,
    NowPlaying,
    Lyrics,
    Equalizer,
    Settings,
    Artists,
    ArtistDetail,
    MusicTracker,
    Aod,
    TrackDetails
}

// Keep Greeting for screenshot test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
