package com.example.budgify.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.LoanType
import com.example.budgify.factory.ViewModelFactory
import com.example.budgify.routes.ARG_INITIAL_LOAN_TYPE
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.CategoriesScreen
import com.example.budgify.screen.CredDebManagementScreen
import com.example.budgify.screen.CreditsDebtsScreen
import com.example.budgify.screen.Homepage
import com.example.budgify.screen.ObjectivesManagementScreen
import com.example.budgify.screen.ObjectivesScreen
import com.example.budgify.screen.Settings
import com.example.budgify.screen.TransactionsScreen
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.example.budgify.utils.getSavedPinFromContext
import com.example.budgify.viewmodel.CategoriesViewModel
import com.example.budgify.viewmodel.CredDebManagementViewModel
import com.example.budgify.viewmodel.CreditsDebitsViewModel
import com.example.budgify.viewmodel.HomepageViewModel
import com.example.budgify.viewmodel.ObjectivesManagementViewModel
import com.example.budgify.viewmodel.ObjectivesViewModel
import com.example.budgify.viewmodel.SettingsViewModel
import com.example.budgify.viewmodel.TransactionsViewModel

@Composable
fun NavGraph(
    viewModel: FinanceViewModel,
    themePreferenceManager: ThemePreferenceManager,
    onThemeChange: (AppTheme) -> Unit,
    startDestination: String,
    onForgotPinClicked: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val showAddTransactionDialog by viewModel.showAddTransactionDialog.collectAsStateWithLifecycle()
    val showAddObjectiveDialog by viewModel.showAddObjectiveDialog.collectAsStateWithLifecycle()
    val showAddLoanDialog by viewModel.showAddLoanDialog.collectAsStateWithLifecycle()

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onDismissAddTransactionDialog() },
            onTransactionAdded = { viewModel.onDismissAddTransactionDialog() }
        )
    }

    if (showAddObjectiveDialog) {
        AddObjectiveDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onDismissAddObjectiveDialog() },
            onObjectiveAdded = { viewModel.onDismissAddObjectiveDialog() }
        )
    }

    if (showAddLoanDialog) {
        AddLoanDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onDismissAddLoanDialog() },
            onLoanAdded = { viewModel.onDismissAddLoanDialog() }
        )
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ScreenRoutes.AccessPin.route) {
            PinEntryScreen(
                onPinCorrect = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(ScreenRoutes.AccessPin.route) { inclusive = true }
                    }
                },
                onForgotPin = onForgotPinClicked
            )
        }

        composable(ScreenRoutes.Categories.route) {
            val factory = ViewModelFactory(viewModel)
            val categoriesViewModel: CategoriesViewModel = viewModel(factory = factory)
            CategoriesScreen(navController, viewModel, categoriesViewModel)
        }
        composable(ScreenRoutes.Home.route) {
            val factory = ViewModelFactory(viewModel)
            val homepageViewModel: HomepageViewModel = viewModel(factory = factory)
            Homepage(navController, viewModel, homepageViewModel)
        }
        composable(ScreenRoutes.Objectives.route) {
            val factory = ViewModelFactory(viewModel)
            val objectivesViewModel: ObjectivesViewModel = viewModel(factory = factory)
            ObjectivesScreen(navController, viewModel, objectivesViewModel)
        }
        composable(ScreenRoutes.ObjectivesManagement.route) {
            val factory = ViewModelFactory(viewModel)
            val objectivesManagementViewModel: ObjectivesManagementViewModel = viewModel(factory = factory)
            ObjectivesManagementScreen(navController, viewModel, objectivesManagementViewModel)
        }
        composable(ScreenRoutes.Settings.route) {
            val factory = ViewModelFactory(viewModel, themePreferenceManager)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            Settings(navController, viewModel, settingsViewModel, onThemeChange)
        }
        composable(ScreenRoutes.Transactions.route) {
            val factory = ViewModelFactory(viewModel)
            val transactionsViewModel: TransactionsViewModel = viewModel(factory = factory)
            TransactionsScreen(navController, viewModel, transactionsViewModel)
        }
        composable(ScreenRoutes.CredDeb.route) {
            val factory = ViewModelFactory(viewModel)
            val creditsDebitsViewModel: CreditsDebitsViewModel = viewModel(factory = factory)
            CreditsDebtsScreen(navController, viewModel, creditsDebitsViewModel)
        }
        composable(
            route = ScreenRoutes.CredDebManagement.route,
            arguments = listOf(
                navArgument(ARG_INITIAL_LOAN_TYPE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialLoanTypeName = backStackEntry.arguments?.getString(ARG_INITIAL_LOAN_TYPE)
            val initialLoanType = try {
                initialLoanTypeName?.let { LoanType.valueOf(it.uppercase()) }
            } catch (e: IllegalArgumentException) {
                Log.e("NavGraph", "Invalid LoanType argument: $initialLoanTypeName", e)
                null
            }
            val factory = ViewModelFactory(viewModel)
            val credDebManagementViewModel: CredDebManagementViewModel = viewModel(factory = factory)
            CredDebManagementScreen(
                navController = navController,
                viewModel = viewModel,
                credDebManagementViewModel = credDebManagementViewModel,
                initialSelectedLoanType = initialLoanType
            )
        }
    }
}

@Composable
fun PinEntryScreen(
    onPinCorrect: () -> Unit,
    onForgotPin: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enter Access PIN", style = MaterialTheme.typography.headlineSmall)

            TextField(
                value = enteredPin,
                onValueChange = {
                    if (it.length <= 6) {
                        enteredPin = it
                    }
                    errorMessage = null
                },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val savedPin = getSavedPinFromContext(context)
                    if (enteredPin == savedPin && savedPin != null) {
                        onPinCorrect()
                    } else {
                        errorMessage = "Incorrect PIN"
                        enteredPin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enteredPin.isNotBlank()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onForgotPin) {
                Text("Forgot PIN?")
            }
        }
    }
}
