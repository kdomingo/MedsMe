package com.domtech.medtracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.domtech.medtracker.ui.screens.EditMedicationScreen
import com.domtech.medtracker.ui.screens.MedicationListScreen
import com.domtech.medtracker.ui.screens.MedicationDetailsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object List : Route
    @Serializable data object Add : Route
    @Serializable data class Edit(val id: Long) : Route
    @Serializable data class Details(val id: Long) : Route
}

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Route.List,
        modifier = modifier,
    ) {
        composable<Route.List> {
            MedicationListScreen(
                onAdd = { nav.navigate(Route.Add) },
                onOpen = { id -> nav.navigate(Route.Details(id)) },
            )
        }

        composable<Route.Add> {
            EditMedicationScreen(
                onDone = { nav.popBackStack() },
            )
        }

        composable<Route.Edit> { entry ->
            EditMedicationScreen(
                onDone = { nav.popBackStack() },
            )
        }

        composable<Route.Details> { entry ->
            val details: Route.Details = entry.toRoute()
            MedicationDetailsScreen(
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Route.Edit(details.id)) },
            )
        }
    }
}