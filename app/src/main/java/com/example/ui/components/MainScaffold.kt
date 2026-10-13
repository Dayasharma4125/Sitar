package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.PlayerUiState

/**
 * M3 Primary Navigation Destinations.
 */
enum class MainNavTab(
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Default.Home, "nav_tab_home"),
    SEARCH("Search", Icons.Default.Search, "nav_tab_search"),
    ARTISTS("Artists", Icons.Default.Person, "nav_tab_artists")
}

/**
 * Strict Material 3 Global Scaffold Layout:
 * 1. Top Section: Hosts standard M3 DockedSearchBar or TopAppBar without legacy tab rows.
 * 2. Floating Bottom Bar System (Stacked):
 *    - In bottomBar slot: a single Column with navigationBarsPadding() and bottom padding 16.dp.
 *    - Top Element: Split-Pill FloatingMiniPlayer positioned above navigation bar.
 *    - Bottom Element: Floating M3 NavigationBar wrapped in an elevated Surface using
 *      M3 surfaceContainer background, RoundedCornerShape(28.dp), and standard NavigationBarItem pill indicators.
 * 3. Overlap Prevention: Scaffold passes innerPadding directly to screens so LazyColumn scrolls completely clear.
 */
@Composable
fun MainScaffold(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    playerUiState: PlayerUiState,
    onMiniPlayerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit = {},
    showBottomBars: Boolean = true,
    isAmoledBlack: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = if (isAmoledBlack) Color(0xFF000000) else MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBars,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Element: Split-Pill Mini Player
                    if (playerUiState.currentTrack.id.isNotEmpty()) {
                        FloatingMiniPlayer(
                            uiState = playerUiState,
                            onMiniPlayerClick = onMiniPlayerClick,
                            onPlayPauseClick = onPlayPauseClick,
                            onSkipNextClick = onSkipNextClick,
                            onSkipPreviousClick = onSkipPreviousClick,
                            isAmoledBlack = isAmoledBlack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Bottom Element: Floating M3 Navigation Bar
                    FloatingM3NavigationBar(
                        currentTab = currentTab,
                        onTabSelected = onTabSelected,
                        isAmoledBlack = isAmoledBlack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        content = { innerPadding ->
            content(innerPadding)
        }
    )
}

/**
 * Floating standard Material 3 NavigationBar component.
 * Wrapped in an elevated Surface with extra-large pill radius (28.dp) and M3 surfaceContainer colors.
 */
@Composable
fun FloatingM3NavigationBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    isAmoledBlack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isAmoledBlack) {
        Color(0xFF0C0E14)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
        modifier = modifier.testTag("floating_m3_nav_bar")
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
            MainNavTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag(tab.testTag)
                )
            }
        }
    }
}
