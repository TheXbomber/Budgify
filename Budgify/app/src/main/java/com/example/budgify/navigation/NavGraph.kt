package com.example.budgify.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.budgify.applicationlogic.FinanceApplication
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.auth.AuthViewModelFactory
import com.example.budgify.auth.LoginScreen
import com.example.budgify.auth.RegistrationScreen
import com.example.budgify.entities.LoanType
import com.example.budgify.factory.ViewModelFactory
import com.example.budgify.routes.ARG_INITIAL_LOAN_TYPE
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.*
import com.example.budgify.userpreferences.AppTheme
import com.example.budgify.userpreferences.ThemePreferenceManager
import com.example.budgify.utils.getSavedPinFromContext
import com.example.budgify.viewmodel.*

@Composable
fun NavGraph(
    themePreferenceManager: ThemePreferenceManager,
    onThemeChange: (AppTheme) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val application = context.applicationContext as FinanceApplication
    val financeViewModel: FinanceViewModel = viewModel(
        factory = FinanceViewModel.FinanceViewModelFactory(
            application.repository,
            application.authService
        )
    )
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            application.database.categoryDao(),
            application.authService
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }


    val showAddTransactionDialog by financeViewModel.showAddTransactionDialog.collectAsStateWithLifecycle()
    val showAddObjectiveDialog by financeViewModel.showAddObjectiveDialog.collectAsStateWithLifecycle()
    val showAddLoanDialog by financeViewModel.showAddLoanDialog.collectAsStateWithLifecycle()

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            viewModel = financeViewModel,
            onDismiss = { financeViewModel.onDismissAddTransactionDialog() },
            onTransactionAdded = { financeViewModel.onDismissAddTransactionDialog() }
        )
    }

    if (showAddObjectiveDialog) {
        AddObjectiveDialog(
            viewModel = financeViewModel,
            onDismiss = { financeViewModel.onDismissAddObjectiveDialog() },
            onObjectiveAdded = { financeViewModel.onDismissAddObjectiveDialog() }
        )
    }

    if (showAddLoanDialog) {
        AddLoanDialog(
            viewModel = financeViewModel,
            onDismiss = { financeViewModel.onDismissAddLoanDialog() },
            onLoanAdded = { financeViewModel.onDismissAddLoanDialog() }
        )
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(ScreenRoutes.Pin.route) {
            PinScreen(
                onPinVerified = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(ScreenRoutes.Pin.route) { inclusive = true }
                    }
                },
                financeViewModel = financeViewModel
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
                onLoginSuccess = {
                    financeViewModel.onUserLoggedIn()
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("registration") {
            RegistrationScreen(
                navController = navController,
                authViewModel = authViewModel,
                onRegistrationSuccess = {
                    financeViewModel.onUserLoggedIn()
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo("registration") { inclusive = true }
                    }
                }
            )
        }
        composable(ScreenRoutes.Categories.route) {
            val factory = ViewModelFactory(financeViewModel)
            val categoriesViewModel: CategoriesViewModel = viewModel(factory = factory)
            CategoriesScreen(navController, financeViewModel, categoriesViewModel, authViewModel)
        }
        composable(ScreenRoutes.Home.route) {
            val factory = ViewModelFactory(financeViewModel)
            val homepageViewModel: HomepageViewModel = viewModel(factory = factory)
            Homepage(navController, financeViewModel, homepageViewModel, authViewModel)
        }
        composable(ScreenRoutes.Objectives.route) {
            val factory = ViewModelFactory(financeViewModel)
            val objectivesViewModel: ObjectivesViewModel = viewModel(factory = factory)
            ObjectivesScreen(navController, financeViewModel, objectivesViewModel, authViewModel)
        }
        composable(ScreenRoutes.ObjectivesManagement.route) {
            val factory = ViewModelFactory(financeViewModel)
            val objectivesManagementViewModel: ObjectivesManagementViewModel = viewModel(factory = factory)
            ObjectivesManagementScreen(navController, financeViewModel, objectivesManagementViewModel, authViewModel)
        }
        composable(ScreenRoutes.Settings.route) {
            val factory = ViewModelFactory(financeViewModel, themePreferenceManager)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            Settings(navController, financeViewModel, settingsViewModel, onThemeChange, authViewModel)
        }
        composable(ScreenRoutes.Transactions.route) {
            val factory = ViewModelFactory(financeViewModel)
            val transactionsViewModel: TransactionsViewModel = viewModel(factory = factory)
            TransactionsScreen(navController, financeViewModel, transactionsViewModel, authViewModel)
        }
        composable(ScreenRoutes.CredDeb.route) {
            val factory = ViewModelFactory(financeViewModel)
            val creditsDebitsViewModel: CreditsDebitsViewModel = viewModel(factory = factory)
            CreditsDebtsScreen(navController, financeViewModel, creditsDebitsViewModel, authViewModel)
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
            val factory = ViewModelFactory(financeViewModel)
            val credDebManagementViewModel: CredDebManagementViewModel = viewModel(factory = factory)
            CredDebManagementScreen(
                navController = navController,
                viewModel = financeViewModel,
                credDebManagementViewModel = credDebManagementViewModel,
                authViewModel = authViewModel,
                initialSelectedLoanType = initialLoanType
            )
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val user by authViewModel.user.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val savedPin = remember { getSavedPinFromContext(context) }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LaunchedEffect(user) {
            val destination = if (user != null) {
                if (savedPin != null) ScreenRoutes.Pin.route else ScreenRoutes.Home.route
            } else {
                "login"
            }
            navController.navigate(destination) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
}