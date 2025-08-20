package com.example.kotlinai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinai.ui.theme.KotlinAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinAITheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(navController = navController, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

object Destinations {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Main = "main"
}

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

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Splash Screen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onContinue) {
                Text(text = "Continue")
            }
        }
    }
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Onboarding Screen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onContinue) {
                Text(text = "Get started")
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold {
        Text(
            text = "Main Screen",
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}