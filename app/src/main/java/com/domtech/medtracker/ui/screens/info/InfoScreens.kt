package com.domtech.medtracker.ui.screens.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.domtech.medtracker.R
import com.domtech.medtracker.ui.components.MedsMeScaffold

@Composable
fun InfoScreen(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    MedsMeScaffold(
        title = title,
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CreditsScreen(onBack: () -> Unit) {
    InfoScreen(
        title = stringResource(R.string.credits),
        content = stringResource(R.string.credits_content),
        onBack = onBack
    )
}

@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    InfoScreen(
        title = stringResource(R.string.terms_and_conditions),
        content = stringResource(R.string.terms_content),
        onBack = onBack
    )
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    InfoScreen(
        title = stringResource(R.string.privacy_policy),
        content = stringResource(R.string.privacy_content),
        onBack = onBack
    )
}
