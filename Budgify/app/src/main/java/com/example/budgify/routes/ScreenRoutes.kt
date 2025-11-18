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
    AccessPin("access_pin", "Pin", 999999994); // Semicolon if you add more to the enum, like companion object functions

    // Helper function to build the actual route string for navigation
    // This is particularly useful for routes with arguments.
    fun buildRoute(vararg args: Pair<String, String>): String {
        var finalRoute = route
        args.forEach { (key, value) ->
            finalRoute = finalRoute.replace("{$key}", value, ignoreCase = true)
        }
        // For optional arguments, if a value wasn't provided, we might need to clean up the route
        // This example assumes the NavController handles the "?" for optional args correctly when the value is missing.
        // Or, more simply for this specific case:
        if (this == CredDebManagement && args.none { it.first == ARG_INITIAL_LOAN_TYPE }) {
            // If navigating to CredDebManagement without the arg, remove the placeholder part
            return route.substringBefore("/{")
        }
        return finalRoute
    }

    // Simplified way to get the base route for NavHost if the definition includes arguments
    val baseRoute: String
        get() = route.substringBefore("/{")


    // If you only have one argument for CredDebManagement, a specific builder is simpler:
    companion object {
        fun credDebManagementRouteWithArg(loanType: com.example.budgify.entities.LoanType? = null): String {
            return if (loanType != null) {
                ScreenRoutes.CredDebManagement.route.replace("{$ARG_INITIAL_LOAN_TYPE}", loanType.name)
            } else {
                // If no arg, return the route without the optional part.
                // NavController handles "route/{arg}?" by matching "route" or "route/value"
                ScreenRoutes.CredDebManagement.route.substringBefore("/{")
                // Or, more robustly for NavHost, ensure the pattern is always available:
                // ScreenRoutes.CredDebManagement.routeDefinition
            }
        }
    }
}