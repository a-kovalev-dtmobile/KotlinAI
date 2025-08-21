package com.example.kotlinai.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import com.example.kotlinai.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kotlinai.screens.main.tabs.favorites.FavoritesScreen
import com.example.kotlinai.screens.main.tabs.history.HistoryScreen
import com.example.kotlinai.screens.main.tabs.personal.PersonalScreen
import com.example.kotlinai.screens.main.tabs.scan.ScanScreen
import com.example.kotlinai.screens.main.tabs.settings.SettingsScreen

private enum class Tab(val title: String) {
    Personal("Personal"),
    History("History"),
    Scan("Scan"),
    Favorites("Favorites"),
    Settings("Settings")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.Personal) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.Personal,
                    onClick = { selectedTab = Tab.Personal },
                    icon = {
                        if (selectedTab == Tab.Personal) {
                            Icon(
                                painterResource(id = R.drawable.ic_generate),
                                contentDescription = "Personal",
                                tint = colorResource(id = R.color.medium_turquoise)
                            )
                        } else {
                            Icon(painterResource(id = R.drawable.ic_generate), contentDescription = "Personal")
                        }
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.History,
                    onClick = { selectedTab = Tab.History },
                    icon = {
                        if (selectedTab == Tab.History) {
                            Icon(
                                painterResource(id = R.drawable.ic_history),
                                contentDescription = "History",
                                tint = colorResource(id = R.color.spiro_disco_ball)
                            )
                        } else {
                            Icon(painterResource(id = R.drawable.ic_history), contentDescription = "History")
                        }
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Scan,
                    onClick = { selectedTab = Tab.Scan },
                    icon = {
                        if (selectedTab == Tab.Scan) {
                            Icon(
                                painterResource(id = R.drawable.ic_scan),
                                contentDescription = "Scan",
                                tint = colorResource(id = R.color.very_light_blue)
                            )
                        } else {
                            Icon(painterResource(id = R.drawable.ic_scan), contentDescription = "Scan")
                        }
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Favorites,
                    onClick = { selectedTab = Tab.Favorites },
                    icon = {
                        if (selectedTab == Tab.Favorites) {
                            Icon(
                                painterResource(id = R.drawable.ic_favorites),
                                contentDescription = "Favorites",
                                tint = colorResource(id = R.color.big_foot_feet)
                            )
                        } else {
                            Icon(painterResource(id = R.drawable.ic_favorites), contentDescription = "Favorites")
                        }
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    selected = selectedTab == Tab.Settings,
                    onClick = { selectedTab = Tab.Settings },
                    icon = {
                        if (selectedTab == Tab.Settings) {
                            Icon(
                                painterResource(id = R.drawable.ic_settings),
                                contentDescription = "Settings",
                                tint = colorResource(id = R.color.heliotrope)
                            )
                        } else {
                            Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                        }
                    },
                    alwaysShowLabel = false
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                Tab.Personal -> PersonalScreen()
                Tab.History -> HistoryScreen()
                Tab.Scan -> ScanScreen()
                Tab.Favorites -> FavoritesScreen()
                Tab.Settings -> SettingsScreen()
            }
        }
    }
}
