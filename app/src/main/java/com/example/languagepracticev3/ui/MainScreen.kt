// app/src/main/java/com/example/languagepracticev3/ui/MainScreen.kt
package com.example.languagepracticev3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.ui.screens.compare.CompareScreen
import com.example.languagepracticev3.ui.screens.experiment.ExperimentScreen
import com.example.languagepracticev3.ui.screens.library.LibraryScreen
import com.example.languagepracticev3.ui.screens.mindsetlab.MindsetLabScreen
import com.example.languagepracticev3.ui.screens.poetrylab.PoetryLabScreen
import com.example.languagepracticev3.ui.screens.practice.PracticeScreen
import com.example.languagepracticev3.ui.screens.routes.RoutesScreen
import com.example.languagepracticev3.ui.screens.settings.SettingsScreen
import com.example.languagepracticev3.ui.screens.workbench.WorkbenchScreen
import com.example.languagepracticev3.ui.screens.selfquestioning.SelfQuestioningScreen  // ★追加
import com.example.languagepracticev3.viewmodel.MainViewModel
import com.example.languagepracticev3.viewmodel.Screen

data class NavigationItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val navigationItems = listOf(
        NavigationItem(Screen.WORKBENCH, "作業台", Icons.Default.Build),
        NavigationItem(Screen.ROUTES, "ルート", Icons.Default.Route),
        NavigationItem(Screen.PRACTICE, "練習", Icons.Default.FitnessCenter),
        NavigationItem(Screen.SELF_QUESTIONING, "自問自答", Icons.Default.Psychology, MaterialTheme.colorScheme.tertiary),  // ★追加
        NavigationItem(Screen.EXPERIMENT, "実験", Icons.Default.Science),
        NavigationItem(Screen.LIBRARY, "ライブラリ", Icons.Default.LibraryBooks),
        NavigationItem(Screen.COMPARE, "比較", Icons.Default.Compare),
        NavigationItem(Screen.POETRY_LAB, "PoetryLab", Icons.Default.TheaterComedy, MaterialTheme.colorScheme.secondary),
        NavigationItem(Screen.MINDSET_LAB, "MindsetLab", Icons.Default.Psychology, MaterialTheme.colorScheme.primary),
        NavigationItem(Screen.SETTINGS, "設定", Icons.Default.Settings)
    )

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(220.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Language\nPractice",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                navigationItems.forEachIndexed { index, item ->
                    // ★修正: index == 7 に変更（PoetryLab/MindsetLabの前に区切り線）
                    if (index == 7) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = item.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                color = item.color ?: MaterialTheme.colorScheme.onSurface
                            )
                        },
                        selected = currentScreen == item.screen,
                        onClick = { viewModel.navigateTo(item.screen) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.SETTINGS -> SettingsScreen(
                    onNavigateToLibrary = {
                        viewModel.navigateTo(Screen.LIBRARY)
                    }
                )
                Screen.WORKBENCH -> WorkbenchScreen()
                Screen.ROUTES -> RoutesScreen()
                Screen.PRACTICE -> PracticeScreen()
                Screen.EXPERIMENT -> ExperimentScreen()
                Screen.LIBRARY -> LibraryScreen()
                Screen.COMPARE -> CompareScreen()
                Screen.POETRY_LAB -> PoetryLabScreen()
                Screen.MINDSET_LAB -> MindsetLabScreen()
                Screen.SELF_QUESTIONING -> SelfQuestioningScreen()  // ★追加
            }
        }
    }
}