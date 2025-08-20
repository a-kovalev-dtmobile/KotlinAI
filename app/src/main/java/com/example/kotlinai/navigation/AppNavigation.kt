package com.example.kotlinai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kotlinai.screens.splash.SplashScreen
import com.example.kotlinai.screens.onboarding.OnboardingScreen
import com.example.kotlinai.screens.main.MainScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Destinations.Splash,
        modifier = modifier
    ) {
        composable(Destinations.Splash) {
            SplashScreen(onContinue = { navController.navigate(Destinations.Onboarding) })
        }
        composable(Destinations.Onboarding) {
            OnboardingScreen(onContinue = { navController.navigate(Destinations.Main) })
        }
        composable(Destinations.Main) {
            MainScreen()
        }
    }
}
