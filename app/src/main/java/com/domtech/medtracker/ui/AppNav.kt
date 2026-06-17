package com.domtech.medtracker.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.domtech.medtracker.R
import com.domtech.medtracker.ui.screens.EditMedicationScreen
import com.domtech.medtracker.ui.screens.MedicationListScreen
import com.domtech.medtracker.ui.screens.MedicationDetailsScreen
import com.domtech.medtracker.ui.screens.info.CreditsScreen
import com.domtech.medtracker.ui.screens.info.PrivacyPolicyScreen
import com.domtech.medtracker.ui.screens.info.TermsAndConditionsScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object List : Route
    @Serializable data object Add : Route
    @Serializable data class Edit(val id: Long) : Route
    @Serializable data class Details(val id: Long) : Route
    @Serializable data object Credits : Route
    @Serializable data object TermsAndConditions : Route
    @Serializable data object PrivacyPolicy : Route
}

@Composable
fun AppNav(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.medications)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Route.List) {
                            popUpTo(Route.List) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.credits)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Route.Credits)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.terms_and_conditions)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Route.TermsAndConditions)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.privacy_policy)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Route.PrivacyPolicy)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        modifier = modifier
    ) {
        NavHost(
            navController = nav,
            startDestination = Route.List,
        ) {
            composable<Route.List> {
                MedicationListScreen(
                    onAdd = { nav.navigate(Route.Add) },
                    onOpen = { id -> nav.navigate(Route.Details(id)) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            composable<Route.Add> {
                EditMedicationScreen(
                    onDone = { nav.popBackStack() },
                )
            }

            composable<Route.Edit> {
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

            composable<Route.Credits> {
                CreditsScreen(onBack = { nav.popBackStack() })
            }

            composable<Route.TermsAndConditions> {
                TermsAndConditionsScreen(onBack = { nav.popBackStack() })
            }

            composable<Route.PrivacyPolicy> {
                PrivacyPolicyScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
