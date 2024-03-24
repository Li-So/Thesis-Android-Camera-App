package de.yanneckreiss.cameraxtutorial.ui.features.camera.no_permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoPermissionScreen(
    onRequestPermission: () -> Unit
) {

    NoPermissionContent(
        onRequestPermission = onRequestPermission
    )
}

@Composable
private fun NoPermissionContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LaunchedEffect(key1 = true) {
            onRequestPermission()
        }

        Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = "settings_icon",
            modifier = Modifier
                .size(60.dp)
        )

        Text(
            text = "Couldn't use Camera.",
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "To use the camera, please enable camera access in your phone's Settings",
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun Preview_NoPermissionContent() {
    NoPermissionContent(
        onRequestPermission = {}
    )
}
