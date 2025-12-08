package com.example.budgify.screen

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Account
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.HomepageViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val smallTextStyle = TextStyle(fontSize = 11.sp)

val items = listOf(
    ScreenRoutes.Transactions,
    ScreenRoutes.Objectives,
    ScreenRoutes.Adding,
    ScreenRoutes.CredDeb,
    ScreenRoutes.Categories
)

@Composable
fun Homepage(navController: NavController, viewModel: FinanceViewModel, homepageViewModel: HomepageViewModel) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Home.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by homepageViewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    val oneTimeSnackbarMessage by viewModel.oneTimeSnackbarMessage.collectAsStateWithLifecycle()

    LaunchedEffect(oneTimeSnackbarMessage) {
        oneTimeSnackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            homepageViewModel.onSnackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
        bottomBar = {
            BottomBar(
                navController,
                viewModel
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    GraficiBox(
                        uiState = uiState,
                        onChartTypeChanged = { homepageViewModel.onChartTypeChanged() },
                        onTransactionTypeChanged = { homepageViewModel.onChartTransactionTypeChanged() },
                        onFilterClicked = { homepageViewModel.onShowAccountFilterDialog() },
                        onChartLongClicked = { homepageViewModel.onChartLongClicked(it) }
                    )
                }
                item {
                    ContiBox(
                        uiState = uiState,
                        onToggleBalanceVisibility = { homepageViewModel.onToggleBalanceVisibility() },
                        onAccountLongClicked = { homepageViewModel.onAccountLongClicked(it) },
                        onAddAccountClicked = { homepageViewModel.onAddAccountClicked() }
                    )
                }
                item {
                    LastTransactionBox(
                        uiState = uiState,
                        onTransactionLongClicked = { homepageViewModel.onTransactionLongClicked(it) }
                    )
                }
            }
        }
    }

    if (uiState.showAddAccountDialog) {
        AddAccountDialog(
            viewModel = homepageViewModel,
            onDismiss = { homepageViewModel.onDismissAddAccountDialog() }
        )
    }
    if (uiState.showAccountActionChoiceDialog) {
        AccountActionChoiceDialog(
            account = uiState.accountToAction!!,
            onDismiss = { homepageViewModel.onDismissAccountActionChoiceDialog() },
            onEdit = { homepageViewModel.onEditAccountClicked() },
            onDelete = { homepageViewModel.onDeleteAccountClicked() }
        )
    }
    if (uiState.showEditAccountDialog) {
        EditAccountDialog(
            accountToEdit = uiState.accountToAction!!,
            viewModel = homepageViewModel,
            onDismiss = { homepageViewModel.onDismissEditAccountDialog() }
        )
    }
    if (uiState.showDeleteAccountConfirmationDialog) {
        DeleteConfirmationDialog(
            title = "Confirm Deletion",
            text = "Are you sure you want to delete the account \"${uiState.accountToAction?.title}\"?\nAll transactions related to this account will also be deleted",
            onDismiss = { homepageViewModel.onDismissDeleteAccountConfirmationDialog() },
            onConfirm = { homepageViewModel.onConfirmDeleteAccount() }
        )
    }
    if (uiState.showTransactionActionChoiceDialog) {
        TransactionActionChoiceDialog(
            transactionDescription = uiState.transactionToAction?.description ?: "",
            onDismiss = { homepageViewModel.onDismissTransactionActionChoiceDialog() },
            onEdit = { homepageViewModel.onEditTransactionClicked() },
            onDelete = { homepageViewModel.onDeleteTransactionClicked() }
        )
    }
    if (uiState.showEditTransactionDialog) {
        EditTransactionDialog(
            transaction = uiState.transactionToAction!!,
            viewModel = homepageViewModel,
            onDismiss = { homepageViewModel.onDismissEditTransactionDialog() }
        )
    }
    if (uiState.showDeleteTransactionConfirmationDialog) {
        DeleteConfirmationDialog(
            title = "Confirm Deletion",
            text = "Are you sure you want to delete this transaction: \"${uiState.transactionToAction?.description}\"?",
            onDismiss = { homepageViewModel.onDismissDeleteTransactionConfirmationDialog() },
            onConfirm = { homepageViewModel.onConfirmDeleteTransaction() }
        )
    }
    if (uiState.showAccountFilterDialog) {
        AccountFilterDialog(
            allAccounts = uiState.accounts,
            selectedAccountIds = uiState.selectedChartAccountIds,
            onSelectionChanged = { homepageViewModel.onChartAccountFilterChanged(it) },
            onDismiss = { homepageViewModel.onDismissAccountFilterDialog() }
        )
    }
    if (uiState.showChartDetailDialog) {
        ChartDetailDialog(
            account = uiState.selectedAccountForDetail!!,
            chartType = uiState.chartType,
            transactionType = uiState.chartTransactionType,
            allTransactions = uiState.transactions,
            onDismiss = { homepageViewModel.onDismissChartDetailDialog() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transactionWithDetails: TransactionWithDetails,
    onClick: (MyTransaction) -> Unit,
    onLongClick: (MyTransaction) -> Unit
) {
    val myTransaction = transactionWithDetails.transaction
    val account = transactionWithDetails.account
    val category = transactionWithDetails.category

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { onClick(myTransaction) },
                onLongClick = { onLongClick(myTransaction) }
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            val formattedAmount = String.format(java.util.Locale.US, "%.2f", myTransaction.amount)
            val amountText = "${if (myTransaction.type == TransactionType.INCOME) "+" else "-"}$formattedAmount €"
            val amountColor = if (myTransaction.type == TransactionType.INCOME) Color(red = 0.0f, green = 0.6f, blue = 0.0f) else Color(red = 0.9f, green = 0.0f, blue = 0.0f)

            val formattedDescription2 = buildAnnotatedString {
                withStyle(style = SpanStyle(color = amountColor, fontWeight = FontWeight.Bold)) {
                    append(amountText)
                }
                append("  ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(account.title)
                }
                append(" - ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                }
            }
            Text(
                text = formattedDescription2,
                style = MaterialTheme.typography.bodyMedium
            )
            val formattedDescription1 = buildAnnotatedString {
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(myTransaction.description)
                }
                append("  (")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(category?.desc ?: "Uncategorized")
                }
                append(")")
            }
            Text(
                text = formattedDescription1,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun LastTransactionBox(
    uiState: com.example.budgify.viewmodel.HomepageUiState,
    onTransactionLongClicked: (MyTransaction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Hold on a transaction to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.transactions.isEmpty()) {
                Text("No transactions found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    uiState.transactions.takeLast(5).reversed()
                        .forEach { transactionWithDetails ->
                            TransactionItem(
                                transactionWithDetails = transactionWithDetails,
                                onClick = { },
                                onLongClick = onTransactionLongClicked
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: MyTransaction,
    viewModel: HomepageViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString().replace('.', ',')) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(transaction.categoryId) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(transaction.date) }
    var selectedType by remember { mutableStateOf<TransactionType>(transaction.type) }
    var selectedAccountId by remember { mutableStateOf<Int?>(transaction.accountId) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                )
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (max 30 characters)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            CategoryDropdown(
                categories = uiState.transactions.mapNotNull { it.category }.distinctBy { it.id },
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it }
            )
            Spacer(modifier = Modifier.height(8.dp))

            AccountDropdown(
                accounts = uiState.accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = { selectedAccountId = it },
                isError = false
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { showDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TransactionTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()
                        if (description.isNotBlank() && amountDouble != null && selectedAccountId != null && selectedDate != null) {
                            val updatedTransaction = transaction.copy(
                                accountId = selectedAccountId!!,
                                type = selectedType,
                                date = selectedDate!!,
                                description = description,
                                amount = amountDouble,
                                categoryId = selectedCategoryId
                            )
                            viewModel.updateTransaction(updatedTransaction)
                        }
                    }) {
                    Text("Save Changes")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transaction.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ContiBox(
    uiState: com.example.budgify.viewmodel.HomepageUiState,
    onToggleBalanceVisibility: () -> Unit,
    onAccountLongClicked: (Account) -> Unit,
    onAddAccountClicked: () -> Unit
) {
    val totalBalance = uiState.accounts.sumOf { it.amount }
    val formattedTotAmount = String.format(java.util.Locale.US, "%.2f", totalBalance)
    val totAmountText = "$formattedTotAmount €"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.balancesVisible) "Total Balance: $totAmountText" else "Total Balance: **** €",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onToggleBalanceVisibility) {
                    Icon(
                        imageVector = if (uiState.balancesVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (uiState.balancesVisible) "Hide balances" else "Show balances"
                    )
                }
            }
            Text(
                text = "Hold on an account to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.accounts.isEmpty()) {
                Text(
                    text = "No accounts found. Tap the '+' button to add a new one!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                uiState.accounts.forEach { account ->
                    AccountItem(
                        account = account,
                        balancesVisible = uiState.balancesVisible,
                        onAccountLongClicked = onAccountLongClicked
                    )
                }
                AddAccountItem(onAddAccountClicked)
            }
        }
    }
}

@Composable
fun AddAccountItem(onAddAccountClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(150.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onAddAccountClicked() }
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add Account",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    account: Account,
    balancesVisible: Boolean,
    onAccountLongClicked: (Account) -> Unit
) {
    val formattedAmount = String.format(java.util.Locale.US, "%.2f", account.amount)
    val amountText = "$formattedAmount €"

    Box(modifier = Modifier
        .padding(8.dp)
        .width(150.dp)
        .height(65.dp)
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            onClick = { },
            onLongClick = { onAccountLongClicked(account) }
        )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = account.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = if (balancesVisible) amountText else "**** €", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddAccountDialog(
    viewModel: HomepageViewModel,
    onDismiss: () -> Unit
) {
    var accountTitle by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add New Account",
                    style = MaterialTheme.typography.titleLarge,
                )
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = accountTitle,
                onValueChange = { accountTitle = it },
                label = { Text("Account Name (max 15 characters)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = initialBalance,
                onValueChange = { initialBalance = it },
                label = { Text("Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = accountTitle.trim().isNotBlank() && initialBalance.isNotBlank(),
                    onClick = {
                        val balanceDouble = initialBalance.replace(',', '.').toDoubleOrNull()
                        if (balanceDouble != null) {
                            viewModel.addAccount(accountTitle.trim(), balanceDouble)
                        }
                    }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun EditAccountDialog(
    accountToEdit: Account,
    viewModel: HomepageViewModel,
    onDismiss: () -> Unit
) {
    var accountTitle by remember { mutableStateOf(accountToEdit.title) }
    var currentBalanceDisplayString by remember { mutableStateOf(accountToEdit.amount.toString().replace('.', ',')) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Account", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = accountTitle,
                onValueChange = { accountTitle = it },
                label = { Text("Account Name (max 15 characters)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = currentBalanceDisplayString,
                onValueChange = { currentBalanceDisplayString = it },
                label = { Text("Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        viewModel.updateAccount(accountToEdit, accountTitle, currentBalanceDisplayString)
                    }
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

val pieChartColorsDefaults = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
    Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
    Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
    Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B)
)
val incomeChartColors = listOf(
    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B),
    Color(0xFF009688), Color(0xFF03A9F4), Color(0xFF2196F3), Color(0xFF00BCD4),
    Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFFE91E63)
)
val expenseChartColors = listOf(
    Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF5722),
    Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF03A9F4)
)


data class PieSlice(val categoryName: String, val amount: Double, val color: Color)

enum class ChartType {
    PIE,
    HISTOGRAM
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    slices: List<PieSlice>
) {
    if (slices.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val totalAmount = slices.sumOf { it.amount }
    if (totalAmount == 0.0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    var startAngle = -90f

    Canvas(modifier = modifier) {
        slices.forEach { slice ->
            val sweepAngle = (slice.amount / totalAmount * 360f).toFloat()
            if (sweepAngle > 0f) {
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                )
            }
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryDistributionPieChart(
    title: String,
    transactionType: TransactionType,
    account: Account,
    allTransactions: List<TransactionWithDetails>,
    colors: List<Color>,
    legendItemLimit: Int? = 3
) {
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (categoryDataMap.isEmpty()) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No ${transactionType.name.lowercase()} data",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val sortedEntries = categoryDataMap.entries.sortedByDescending { it.value }

            val pieSlices = sortedEntries.mapIndexedNotNull { index, entry ->
                if (entry.value > 0) {
                    PieSlice(
                        categoryName = entry.key,
                        amount = entry.value,
                        color = colors[index % colors.size]
                    )
                } else null
            }

            if (pieSlices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${transactionType.name.lowercase()} to display",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PieChart(
                    modifier = Modifier.size(90.dp),
                    slices = pieSlices
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChartLegend(
                    items = pieSlices.map { it.categoryName to it.amount },
                    colors = pieSlices.map { it.color },
                    limit = legendItemLimit
                )
            }
        }
    }
}

@Composable
fun ChartLegend(
    items: List<Pair<String, Double>>,
    colors: List<Color>,
    limit: Int?
) {
    val itemsToShow = if (limit != null) items.take(limit) else items
    val colorsToShow = if (limit != null) colors.take(limit) else colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        itemsToShow.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors[index], RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.first}: ${"%.2f".format(item.second)}€",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (limit != null && items.size > limit) {
            Text(
                text = "+ ${items.size - limit} more...",
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GraficiBox(
    uiState: com.example.budgify.viewmodel.HomepageUiState,
    onChartTypeChanged: () -> Unit,
    onTransactionTypeChanged: () -> Unit,
    onFilterClicked: () -> Unit,
    onChartLongClicked: (Account) -> Unit
) {
    val accountsToDisplayInCharts = remember(uiState.accounts, uiState.transactions, uiState.selectedChartAccountIds) {
        if (uiState.selectedChartAccountIds.isEmpty()) {
            emptyList()
        } else {
            uiState.accounts.filter { account ->
                uiState.selectedChartAccountIds.contains(account.id) &&
                        uiState.transactions.any { transactionDetail ->
                            transactionDetail.transaction.accountId == account.id
                        }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onFilterClicked) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Accounts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onTransactionTypeChanged) {
                        Icon(
                            imageVector = if (uiState.chartTransactionType == TransactionType.EXPENSE) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                            contentDescription = "Toggle Expense/Income View",
                            tint = if (uiState.chartTransactionType == TransactionType.EXPENSE) expenseChartColors[0] else incomeChartColors[0]
                        )
                    }
                    IconButton(onClick = onChartTypeChanged) {
                        Icon(
                            imageVector = if (uiState.chartType == ChartType.PIE) Icons.Filled.PieChart else Icons.Filled.BarChart,
                            contentDescription = "Toggle Chart Type",
                        )
                    }
                }
            }
            Text(
                text = "Hold on a chart to expand",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (accountsToDisplayInCharts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (uiState.accounts.isEmpty()) "No accounts yet. Add an account to see charts."
                        else "No accounts with transactions to display for the current filter."
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    accountsToDisplayInCharts.forEach { account ->
                        SingleAccountChartsCard(
                            account = account,
                            chartType = uiState.chartType,
                            displayedTransactionType = uiState.chartTransactionType,
                            allTransactions = uiState.transactions,
                            onLongClick = { onChartLongClicked(account) },
                            showSnackbar = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountFilterDialog(
    allAccounts: List<Account>,
    selectedAccountIds: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var internalSelectedAccountIds by remember { mutableStateOf(selectedAccountIds) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Accounts",
                    style = MaterialTheme.typography.titleLarge,
                )
                XButton(onDismiss)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (allAccounts.isEmpty()) {
                Text("No accounts available to filter.")
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            internalSelectedAccountIds = if (internalSelectedAccountIds.size == allAccounts.size) {
                                emptySet()
                            } else {
                                allAccounts.map { it.id }.toSet()
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = internalSelectedAccountIds.size == allAccounts.size && allAccounts.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            internalSelectedAccountIds = if (isChecked) {
                                allAccounts.map { it.id }.toSet()
                            } else {
                                emptySet()
                            }
                        }
                    )
                    Text("Select All / Deselect All", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(allAccounts.size) { index ->
                        val account = allAccounts[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    internalSelectedAccountIds = if (internalSelectedAccountIds.contains(account.id)) {
                                        internalSelectedAccountIds - account.id
                                    } else {
                                        internalSelectedAccountIds + account.id
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = internalSelectedAccountIds.contains(account.id),
                                onCheckedChange = { isChecked ->
                                    internalSelectedAccountIds = if (isChecked) {
                                        internalSelectedAccountIds + account.id
                                    } else {
                                        internalSelectedAccountIds - account.id
                                    }
                                }
                            )
                            Text(account.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onSelectionChanged(internalSelectedAccountIds)
                        onDismiss()
                    }
                ) {
                    Text("Apply Filter")
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleAccountChartsCard(
    account: Account,
    chartType: ChartType,
    displayedTransactionType: TransactionType,
    allTransactions: List<TransactionWithDetails>,
    onLongClick: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(250.dp)
            .padding(start = 0.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .combinedClickable(
                onClick = { showSnackbar("Hold on the chart to expand") },
                onLongClick = onLongClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = account.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when (chartType) {
            ChartType.PIE -> CategoryDistributionPieChart(
                title = if (displayedTransactionType == TransactionType.EXPENSE) "Expenses" else "Incomes",
                transactionType = displayedTransactionType,
                account = account,
                allTransactions = allTransactions,
                colors = if (displayedTransactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors,
                legendItemLimit = 3
            )
            ChartType.HISTOGRAM -> CategoryDistributionHistogramChart(
                title = if (displayedTransactionType == TransactionType.EXPENSE) "Expenses" else "Incomes",
                transactionType = displayedTransactionType,
                account = account,
                allTransactions = allTransactions,
                colors = if (displayedTransactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors,
                legendItemLimit = 3
            )
        }
    }
}

@Composable
fun ChartDetailDialog(
    account: Account,
    chartType: ChartType,
    transactionType: TransactionType,
    allTransactions: List<TransactionWithDetails>,
    onDismiss: () -> Unit
) {
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
            .entries.sortedByDescending { it.value }
    }

    val colors = if (transactionType == TransactionType.EXPENSE) expenseChartColors else incomeChartColors

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val typeLabel = if (transactionType == TransactionType.EXPENSE) "Expenses" else "Incomes"
                Text(
                    text = "${account.title}: $typeLabel",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                XButton(onDismiss)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (categoryDataMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display.")
                }
            } else {
                when (chartType) {
                    ChartType.PIE -> {
                        val pieSlices = categoryDataMap.mapIndexedNotNull { index, entry ->
                            if (entry.value > 0) PieSlice(entry.key, entry.value, colors[index % colors.size]) else null
                        }
                        PieChart(modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterHorizontally), slices = pieSlices)
                    }
                    ChartType.HISTOGRAM -> {
                        val histogramBars = categoryDataMap.mapIndexedNotNull { index, entry ->
                            if (entry.value > 0) HistogramBarData(entry.key, entry.value, colors[index % colors.size]) else null
                        }
                        HistogramChart(
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth(),
                            bars = histogramBars,
                            showValuesOnBars = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    val legendItems = categoryDataMap.map { it.key to it.value }
                    val legendColors = categoryDataMap.mapIndexed { index, _ -> colors[index % colors.size] }

                    items(legendItems.size) { index ->
                        val item = legendItems[index]
                        val color = legendColors[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.first}: ${"%.2f".format(item.second)}€",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


data class HistogramBarData(val categoryName: String, val amount: Double, val color: Color)


@Composable
fun CategoryDistributionHistogramChart(
    title: String,
    transactionType: TransactionType,
    account: Account,
    allTransactions: List<TransactionWithDetails>,
    colors: List<Color>,
    legendItemLimit: Int? = 3
) {
    val categoryDataMap = remember(allTransactions, account, transactionType) {
        allTransactions
            .filter { it.transaction.accountId == account.id && it.transaction.type == transactionType }
            .groupBy { it.category?.desc ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (categoryDataMap.isEmpty()) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No ${transactionType.name.lowercase()} data",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val sortedEntries = categoryDataMap.entries.sortedByDescending { it.value }

            val histogramBars = sortedEntries.mapIndexedNotNull { index, entry ->
                if (entry.value > 0) {
                    HistogramBarData(
                        categoryName = entry.key,
                        amount = entry.value,
                        color = colors[index % colors.size]
                    )
                } else null
            }

            if (histogramBars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${transactionType.name.lowercase()} to display",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                HistogramChart(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth(),
                    bars = histogramBars,
                    showValuesOnBars = false
                )
                Spacer(modifier = Modifier.height(8.dp))

                ChartLegend(
                    items = histogramBars.map { it.categoryName to it.amount },
                    colors = histogramBars.map { it.color },
                    limit = legendItemLimit
                )
            }
        }
    }
}

@Composable
fun HistogramChart(
    modifier: Modifier = Modifier,
    bars: List<HistogramBarData>,
    barWidthToSpacingRatio: Float = 2f,
    singleBarFixedWidth: Dp = 50.dp,
    showValuesOnBars: Boolean = true,
    valueTextSizeSp: Float = 9f
) {
    if (bars.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data for histogram", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val maxAmount = bars.maxOfOrNull { it.amount } ?: 0.0
    if (maxAmount == 0.0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Amounts are zero", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
        return
    }

    val density = LocalDensity.current
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = density.run { valueTextSizeSp.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val numberOfBars = bars.size
        if (numberOfBars == 1) {
            val bar = bars.first()
            val barWidthPx = singleBarFixedWidth.toPx()
            val startX = (size.width - barWidthPx) / 2
            val barHeightPx = (bar.amount / maxAmount * size.height).toFloat()

            if (barHeightPx > 0f) {
                drawRect(
                    color = bar.color,
                    topLeft = Offset(x = startX, y = size.height - barHeightPx),
                    size = Size(width = barWidthPx, height = barHeightPx)
                )

                if (showValuesOnBars && bar.amount > 0) {
                    val valueText = "%.0f".format(bar.amount)
                    val textY = size.height - barHeightPx - 4.dp.toPx()
                    val textX = startX + barWidthPx / 2
                    if (textY > textPaint.textSize) {
                        drawContext.canvas.nativeCanvas.drawText(valueText, textX, textY, textPaint)
                    }
                }
            }
        } else {
            val totalSlotUnits = barWidthToSpacingRatio + 1
            val slotWidthPx = size.width / (numberOfBars * totalSlotUnits - 1)
            val barActualWidthPx = slotWidthPx * barWidthToSpacingRatio
            val spacingActualPx = slotWidthPx
            var currentX = 0f

            bars.forEach { bar ->
                val barHeightPx = (bar.amount / maxAmount * size.height).toFloat()
                if (barHeightPx > 0f) {
                    drawRect(
                        color = bar.color,
                        topLeft = Offset(x = currentX, y = size.height - barHeightPx),
                        size = Size(width = barActualWidthPx, height = barHeightPx)
                    )

                    if (showValuesOnBars && bar.amount > 0) {
                        val valueText = "%.0f".format(bar.amount)
                        val textY = size.height - barHeightPx - 4.dp.toPx()
                        val textX = currentX + barActualWidthPx / 2
                        if (textY > textPaint.textSize) {
                            drawContext.canvas.nativeCanvas.drawText(valueText, textX, textY, textPaint)
                        }
                    }
                }
                currentX += barActualWidthPx + spacingActualPx
            }
        }
    }
}
