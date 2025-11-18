package com.example.budgify.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.CreditsDebitsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CreditsDebtsScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    creditsDebitsViewModel: CreditsDebitsViewModel
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDeb.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by creditsDebitsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            creditsDebitsViewModel.onSnackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { TopBar(navController, currentRoute) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClickableAmountArea(
                    title = "Total Active Credits",
                    amount = uiState.totalActiveCredits,
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = Color(0xFF4CAF50)
                ) { navController.navigate(ScreenRoutes.credDebManagementRouteWithArg(LoanType.CREDIT)) }
                ClickableAmountArea(
                    title = "Total Active Debts",
                    amount = uiState.totalActiveDebts,
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = Color(0xFFF44336)
                ) { navController.navigate(ScreenRoutes.credDebManagementRouteWithArg(LoanType.DEBT)) }
            }
            Text(
                text = "Tap on one section above for more details",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Recent Active Loans",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "Hold on a loan to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            )

            val displayLoans = uiState.lastThreeLoans.filter { !it.completed }

            if (displayLoans.isEmpty()) {
                Text(if (uiState.lastThreeLoans.any { it.completed }) "No recent active loans. Some loans might be paid." else "No recent loans recorded.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayLoans, key = { loan -> loan.id }) { loan ->
                        LoanRow(
                            loan = loan,
                            onLongPress = { creditsDebitsViewModel.onLoanLongPressed(it) }
                        )
                    }
                }
            }
        }
    }

    uiState.selectedLoan?.let { loan ->
        if (uiState.showActionChoiceDialog) {
            LoanActionChoiceDialog(
                loan = loan,
                onDismiss = { creditsDebitsViewModel.onDismissActionChoiceDialog() },
                onEditClick = { creditsDebitsViewModel.onEditLoanClicked() },
                onDeleteClick = { creditsDebitsViewModel.onDeleteLoanClicked() },
                onCompleteClick = { creditsDebitsViewModel.onCompleteLoanClicked() }
            )
        }

        if (uiState.showDeleteConfirmationDialog) {
            ConfirmLoanDeleteDialog(
                loan = loan,
                onDismiss = { creditsDebitsViewModel.onDismissDeleteConfirmationDialog() },
                onConfirmDelete = { creditsDebitsViewModel.onConfirmDeleteLoan() }
            )
        }

        if (uiState.showEditLoanDialog) {
            EditLoanDialog(
                loanToEdit = loan,
                viewModel = creditsDebitsViewModel,
                onDismiss = { creditsDebitsViewModel.onDismissEditLoanDialog() }
            )
        }

        if (uiState.showAccountSelectionForCompletionDialog) {
            AccountSelectionForCompletionDialog(
                accounts = uiState.accounts,
                hasAccounts = uiState.hasAccounts,
                title = "Select Account",
                itemDescription = "Choose an account to create a transaction for this ${loan.type.name.lowercase()}",
                onDismiss = { creditsDebitsViewModel.onDismissAccountSelectionDialog() },
                onAccountSelected = { creditsDebitsViewModel.onAccountSelectedForCompletion(it) }
            )
        }

        if (uiState.showInsufficientBalanceDialog) {
            InsufficientBalanceDialog(
                requiredAmount = loan.amount,
                accountInfo = uiState.insufficientBalanceAccountInfo,
                onDismiss = { creditsDebitsViewModel.onDismissInsufficientBalanceDialog() }
            )
        }
    }
}


@Composable
fun LoanActionChoiceDialog(
    loan: Loan,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Action for '${loan.desc}'",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XButton(onDismiss)
            }
        },
        text = {
            if (loan.completed) {
                Text("This loan is already ${if (loan.type == LoanType.DEBT) "repaid" else "collected"}. You can only delete it.")
            } else {
                Text("What would you like to do with this loan?")
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!loan.completed) {
                        TextButton(onClick = onEditClick) { Text("Edit") }
                    }
                    TextButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                }
                if (!loan.completed) {
                    TextButton(onClick = onCompleteClick) {
                        Text(if (loan.type == LoanType.DEBT) "Mark as Repaid" else "Mark as Collected")
                    }
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun ConfirmLoanDeleteDialog(
    loan: Loan,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confirm Deletion", modifier = Modifier.weight(1f))
            }
        },
        text = { Text("Are you sure you want to delete '${loan.desc}'? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClickableAmountArea(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    val formattedAmount = String.format(java.util.Locale.US, "%.2f", amount)
    val amountText = "$formattedAmount €"

    Column(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanRow(
    loan: Loan,
    onLongPress: (Loan) -> Unit,
) {
    val amountColor = if (loan.type == LoanType.CREDIT) Color(0xFF4CAF50) else Color(0xFFF44336)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val formattedAmount = String.format(java.util.Locale.US, "%.2f", loan.amount)
    val amountText = "$formattedAmount €"
    val cardAlpha = if (loan.completed) 0.6f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    if (!loan.completed) {
                        onLongPress(loan)
                    }
                }
            )
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = loan.desc,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loan.startDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha)
                    )
                    loan.endDate?.let {
                        Text(
                            text = " - ${it.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha),
                        )
                    }
                }
                if (loan.completed) {
                    Text(
                        text = if (loan.type == LoanType.DEBT) "Repaid" else "Collected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = (if (loan.type == LoanType.DEBT) amountColor else amountColor).copy(alpha = cardAlpha + 0.2f)
                    )
                }
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor.copy(alpha = cardAlpha)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loanToEdit: Loan,
    viewModel: CreditsDebitsViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(loanToEdit.desc) }
    var amountString by remember { mutableStateOf(loanToEdit.amount.toString().replace('.', ',')) }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(loanToEdit.startDate) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(loanToEdit.endDate) }
    var selectedType by remember { mutableStateOf(loanToEdit.type) }
    val loanTypes = remember { LoanType.entries.toList() }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTargetIsStart by remember { mutableStateOf(true) }

    var errorMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        val errors = mutableListOf<String>()
        if (description.isBlank()) {
            errors.add("Description cannot be empty.")
        } else if (description.length > 30) {
            errors.add("Description cannot exceed 30 characters.")
        }
        val amountDouble = amountString.replace(',', '.').toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            errors.add("Please enter a valid positive amount.")
        }
        if (selectedStartDate == null) {
            errors.add("Please select a start date.")
        }
        if (selectedEndDate != null && selectedStartDate != null && selectedEndDate!!.isBefore(selectedStartDate!!)) {
            errors.add("End date cannot be before the start date.")
        }
        errorMessages = errors
        return errors.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Loan",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    XButton(onDismiss)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (max 30 characters)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Amount (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = selectedStartDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            datePickerTargetIsStart = true
                            showDatePickerDialog = true
                        }) {
                            Icon(Icons.Default.CalendarToday, "Select Start Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not set",
                    onValueChange = {},
                    label = { Text("End Date (Optional)") },
                    readOnly = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedEndDate != null) {
                                IconButton(onClick = { selectedEndDate = null }) {
                                    Icon(Icons.Default.Clear, "Clear End Date")
                                }
                            }
                            IconButton(onClick = {
                                datePickerTargetIsStart = false
                                showDatePickerDialog = true
                            }) {
                                Icon(Icons.Default.CalendarToday, "Select End Date")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Type:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedType = type }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(type.name.replaceFirstChar { it.titlecase() })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (validateFields()) {
                                val updatedLoan = loanToEdit.copy(
                                    desc = description.trim(),
                                    amount = amountString.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                    type = selectedType,
                                    startDate = selectedStartDate!!,
                                    endDate = selectedEndDate,
                                    completed = loanToEdit.completed
                                )
                                viewModel.onLoanUpdated(updatedLoan)
                            } else {
                                showValidationErrorDialog = true
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialDateForPicker = if (datePickerTargetIsStart) {
            selectedStartDate ?: LocalDate.now()
        } else {
            selectedEndDate ?: selectedStartDate ?: LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newSelectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            if (datePickerTargetIsStart) {
                                selectedStartDate = newSelectedDate
                                if (selectedEndDate != null && selectedEndDate!!.isBefore(newSelectedDate)) {
                                    selectedEndDate = null
                                }
                            } else {
                                if (selectedStartDate != null && newSelectedDate.isBefore(selectedStartDate!!)) {
                                    selectedEndDate = newSelectedDate
                                } else {
                                    selectedEndDate = newSelectedDate
                                }
                            }
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) }
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = { Text("Validation Error") },
            text = {
                Column {
                    errorMessages.forEach { Text("- $it") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showValidationErrorDialog = false }) { Text("OK") }
            }
        )
    }
}
