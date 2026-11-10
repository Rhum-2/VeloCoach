package com.velocoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.velocoach.app.ui.detail.WorkoutDetailScreen
import com.velocoach.app.ui.editor.WorkoutEditorScreen
import com.velocoach.app.ui.library.LibraryScreen
import com.velocoach.app.ui.navigation.Routes
import com.velocoach.app.ui.plan.PlanScreen
import com.velocoach.app.ui.theme.VeloCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeloCoachTheme {
                VeloCoachApp()
            }
        }
    }
}

@Composable
fun VeloCoachApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.PLAN || currentRoute == Routes.LIBRARY

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.PLAN,
                        onClick = { navigateTopLevel(navController, Routes.PLAN) },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        label = { Text("Semaine") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.LIBRARY,
                        onClick = { navigateTopLevel(navController, Routes.LIBRARY) },
                        icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null) },
                        label = { Text("Séances") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PLAN,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.PLAN) {
                PlanScreen(
                    onOpenWorkout = { workoutId -> navController.navigate(Routes.detail(workoutId)) },
                    onCreateWorkout = { navController.navigate(Routes.editor(-1L)) }
                )
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onOpenWorkout = { workoutId -> navController.navigate(Routes.detail(workoutId)) },
                    onCreateWorkout = { navController.navigate(Routes.editor(-1L)) }
                )
            }
            composable(
                route = Routes.WORKOUT_DETAIL,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { entry ->
                val workoutId = entry.arguments?.getLong("workoutId") ?: -1L
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.editor(id)) }
                )
            }
            composable(
                route = Routes.WORKOUT_EDITOR,
                arguments = listOf(
                    navArgument("workoutId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val workoutId = entry.arguments?.getLong("workoutId") ?: -1L
                WorkoutEditorScreen(
                    workoutId = workoutId,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
