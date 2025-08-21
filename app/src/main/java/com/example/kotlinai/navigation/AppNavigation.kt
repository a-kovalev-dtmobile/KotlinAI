package com.example.kotlinai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.Manifest
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
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
            val context = LocalContext.current
            val hasCamera = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            SplashScreen(onContinue = {
                if (hasCamera) navController.navigate(Destinations.Main) else navController.navigate(Destinations.Onboarding)
            })
        }
        composable(Destinations.Onboarding) {
            OnboardingScreen(onContinue = { navController.navigate(Destinations.Main) })
        }
        composable(Destinations.Main) {
            MainScreen()
        }
    }
}
