package com.example.budgify.routes

import com.example.budgify.R


const val ARG_INITIAL_LOAN_TYPE = "initialLoanType" // Define argument key as a constant

enum class ScreenRoutes(val route: String, val title: String, val icon: Int) {
    Home("home_screen", "Home", 999999999),
    Settings("settings_screen", "Settings", 999999998),
    Transactions("transactions_screen", "Transactions", R.drawable.paid),
    Objectives("objectives_screen", "Goals & Stats", R.drawable.reward),
    ObjectivesManagement("objectives_management_screen", "Manage Goals", 999999997),
    Adding("adding_screen", "Add", R.drawable.add),
    CredDeb("cred_deb_screen", "Loans", R.drawable.cred),
    CredDebManagement(
        route = "cred_deb_management_screen/{$ARG_INITIAL_LOAN_TYPE}?", // Route pattern with optional arg
        title = "Manage Loans",
        icon = 999999995
    ),
    Categories("categories_screen", "Categories", R.drawable.categories),
    Pin("pin_screen", "Pin", 999999994); 

    fun buildRoute(vararg args: Pair<String, String>): String {
        var finalRoute = route
        args.forEach { (key, value) ->
            finalRoute = finalRoute.replace("{$key}", value, ignoreCase = true)
        }
        if (this == CredDebManagement && args.none { it.first == ARG_INITIAL_LOAN_TYPE }) {
            return route.substringBefore("/{")
        }
        return finalRoute
    }

    val baseRoute: String
        get() = route.substringBefore("/{")


    companion object {
        fun credDebManagementRouteWithArg(loanType: com.example.budgify.entities.LoanType? = null): String {
            return if (loanType != null) {
                ScreenRoutes.CredDebManagement.route.replace("{$ARG_INITIAL_LOAN_TYPE}", loanType.name)
            } else {
                ScreenRoutes.CredDebManagement.route.substringBefore("/{")
            }
        }
    }
}