package com.domtech.medtracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.domtech.medtracker.ui.screens.EditMedicationScreen
import com.domtech.medtracker.ui.screens.MedicationListScreen
import com.domtech.medtracker.ui.screens.MedicationDetailsScreen

private object Routes {
    const val List = "list"
    const val Add = "add"
    const val Edit = "edit/{id}"
    const val Details = "details/{id}"
}

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.List,
        modifier = modifier,
    ) {
        composable(Routes.List) {
            MedicationListScreen(
                onAdd = { nav.navigate(Routes.Add) },
                onOpen = { id -> nav.navigate("details/$id") },
            )
        }

        composable(Routes.Add) {
            EditMedicationScreen(
                medId = null,
                onDone = { nav.popBackStack() },
            )
        }

        composable(
            route = Routes.Edit,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            EditMedicationScreen(
                medId = id,
                onDone = { nav.popBackStack() },
            )
        }

        composable(
            route = Routes.Details,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            MedicationDetailsScreen(
                medId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("edit/$id") },
            )
        }
    }
}

