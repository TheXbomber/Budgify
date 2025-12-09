package com.example.budgify

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.budgify.navigation.NavGraph
import com.example.budgify.ui.theme.BudgifyTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themePreferenceManager = ThemePreferenceManager(this)

        setContent {
            var currentTheme by remember { mutableStateOf(themePreferenceManager.getSavedTheme()) }

            BudgifyTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

                    LaunchedEffect(Unit) {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }

                    // You might want to show a more user-friendly message or guide if permissions are not granted
                    if (multiplePermissionsState.allPermissionsGranted) {
                        NavGraph(
                            themePreferenceManager = themePreferenceManager,
                            onThemeChange = { newTheme ->
                                themePreferenceManager.saveTheme(newTheme)
                                currentTheme = newTheme
                            }
                        )
                    } else {
                        // Display a message or a screen requesting permissions
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text("Permissions required for full functionality.")
                            // Optionally, add a button to re-request permissions
                        }
                    }
                }
            }
        }
    }
}
