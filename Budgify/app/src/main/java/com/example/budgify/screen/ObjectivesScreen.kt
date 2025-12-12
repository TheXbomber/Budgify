package com.example.budgify.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.ObjectivesViewModel
import kotlinx.coroutines.launch

@Composable
fun ObjectivesScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    objectivesViewModel: ObjectivesViewModel,
    authViewModel: AuthViewModel
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Objectives.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by objectivesViewModel.uiState.collectAsStateWithLifecycle()
    val user by authViewModel.user.collectAsStateWithLifecycle()
    val accountName = user?.email ?: "Guest" // Get user email or "Guest"

    Scaffold(
        topBar = { TopBar(navController, currentRoute, authViewModel, isHomeScreen = false) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column {

                val explanatoryText =
                    "Here you can check your stats and access goal management.\nReach goals, repay debts or collect credits to gain XP and increase your level.\nBy increasing your level you can unlock new themes!"

                Box(
                    modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = explanatoryText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                ProfileAndLevelSection(
                    profilePicture = rememberVectorPainter(Icons.Filled.Person),
                    currentLevel = uiState.currentLevel,
                    currentXp = uiState.currentXp,
                    xpForNextLevel = uiState.xpForNextLevel,
                    progressToNextLevel = uiState.progressToNextLevel,
                    accountName = accountName
                )

                Spacer(modifier = Modifier.height(5.dp))

                LoanCompletionCountsSection(
                    creditsRepaidCount = uiState.creditsRepaidCount,
                    debtsCollectedCount = uiState.debtsCollectedCount
                )

                Spacer(modifier = Modifier.height(5.dp))

                ObjectiveCountsSection(
                    reachedCount = uiState.reachedCount,
                    unreachedCount = uiState.unreachedCount,
                    navController = navController
                )
            }
        }
    }
}

fun calculateXpForNextLevel(level: Int): Int {
    return 100 * level + (level - 1) * 50 // Example: 100 for L1->L2, 250 L2->L3, 450 L3->L4 etc.
}

@Composable
fun ObjectiveCountsSection(reachedCount: Int, unreachedCount: Int, navController: NavController) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(5.dp)
    ) {
        Text(
            text = "Goals",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Add vertical space
            horizontalArrangement = Arrangement.SpaceEvenly // Distribute space between counts
        ) {
            // Reached Objectives Count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = reachedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Reached",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Unreached Objectives Count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = unreachedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Unreached",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        // Section 4: Manage Objectives Button
        ManageObjectivesButton(navController = navController)
    }
}

@Composable
fun ProfileAndLevelSection(
    profilePicture: Painter,
    currentLevel: Int,
    currentXp: Int, // Add current XP
    xpForNextLevel: Int, // Add XP needed for next level
    progressToNextLevel: Float,
    accountName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp), // Increased padding for better spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Space out elements within this section
    ) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        //Spacer(modifier = Modifier.height(8.dp)) // Add space after "Your Stats"
        Text(
            text = accountName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) { // Align items in the row
            // Profile Picture
            Image(
                painter = profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp) // Slightly smaller
                    .clip(RoundedCornerShape(50)), // Make it circular
                colorFilter = tint(MaterialTheme.colorScheme.onSurface)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column { // Group Level and XP text
                Text(
                    text = "Level $currentLevel",
                    style = MaterialTheme.typography.headlineSmall // More prominent
                )
                Text(
                    text = "$currentXp / $xpForNextLevel XP", // Display XP progress
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        LinearProgressIndicator(
            progress = { progressToNextLevel }, // Pass progress as a lambda
            modifier = Modifier
                .fillMaxWidth(0.8f) // Adjust width of the bar
                .height(12.dp)
                .clip(RoundedCornerShape(5.dp)),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun LoanCompletionCountsSection(creditsRepaidCount: Int, debtsCollectedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth() // Make it fill width like other sections
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp) // Inner padding
    ) {
        Text(
            text = "Loan Settlements", // Or a title you prefer
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp)) // Space between title and counts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Adjusted padding
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Credits Repaid (meaning someone paid you back a credit you gave)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = creditsRepaidCount.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Credits Collected", // "Credits Repaid to You" or "Credits Collected"
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            // Debts Collected (meaning you paid back a debt you owed)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = debtsCollectedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Debts Repaid", // "Debts You Repaid" or "Debts Cleared"
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ManageObjectivesButton(navController: NavController) {
    Button(
        onClick = {
            navController.navigate("objectives_management_screen")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Text("Manage Goals")
    }
}
