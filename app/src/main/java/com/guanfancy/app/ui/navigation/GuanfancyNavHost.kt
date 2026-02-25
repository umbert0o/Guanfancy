package com.guanfancy.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guanfancy.app.ui.screens.calendar.CalendarScreen
import com.guanfancy.app.ui.screens.dashboard.DashboardScreen
import com.guanfancy.app.ui.screens.feedback.FeedbackScreen
import com.guanfancy.app.ui.screens.onboarding.OnboardingScreen
import com.guanfancy.app.ui.screens.settings.SettingsScreen
import com.guanfancy.app.ui.screens.settings.GuanfacineInfoScreen
import com.guanfancy.app.ui.screens.warning.WarningScreen
import com.guanfancy.app.domain.repository.SettingsRepository
import androidx.hilt.navigation.compose.hiltViewModel
import com.guanfancy.app.ui.screens.dashboard.DashboardViewModel
import com.guanfancy.app.ui.screens.feedback.FeedbackViewModel
import com.guanfancy.app.ui.screens.onboarding.OnboardingViewModel
import com.guanfancy.app.ui.screens.settings.SettingsViewModel
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import kotlin.reflect.typeOf

@Composable
fun GuanfancyNavHost(
    navController: NavHostController = rememberNavController(),
    settingsRepository: SettingsRepository = hiltViewModel<DashboardViewModel>().settingsRepository,
    deepLinkIntakeId: Long? = null
) {
    var pendingDeepLink by remember { mutableStateOf(deepLinkIntakeId) }

    val startDestination by produceState<Screen?>(initialValue = null) {
        val warningAccepted = settingsRepository.isWarningAccepted.first()
        val onboardingCompleted = settingsRepository.isOnboardingCompleted.first()
        value = when {
            !warningAccepted -> Screen.Warning
            !onboardingCompleted -> Screen.Onboarding
            deepLinkIntakeId != null -> {
                pendingDeepLink = null
                Screen.Feedback(deepLinkIntakeId)
            }
            else -> Screen.Dashboard
        }
    }

    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink != null && navController.currentDestination?.route != null) {
            navController.navigate(Screen.Feedback(pendingDeepLink!!)) {
                launchSingleTop = true
            }
            pendingDeepLink = null
        }
    }

    startDestination?.let { destination ->
        NavHost(
            navController = navController,
            startDestination = destination
        ) {
        composable<Screen.Warning> {
            WarningScreen(
                onAccept = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Warning) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Onboarding> {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToCalendar = { navController.navigate(Screen.Calendar) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) }
            )
        }

        composable<Screen.Calendar> {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGuanfacineInfo = { navController.navigate(Screen.GuanfacineInfo) },
                onResetApp = {
                    navController.navigate(Screen.Warning) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.GuanfacineInfo> {
            GuanfacineInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Feedback> { backStackEntry ->
            val feedback = backStackEntry.toRoute<Screen.Feedback>()
            FeedbackScreen(
                intakeId = feedback.intakeId,
                onFeedbackComplete = { navController.popBackStack() }
            )
        }
    }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
