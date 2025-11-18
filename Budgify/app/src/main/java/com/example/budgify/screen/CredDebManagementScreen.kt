package com.example.budgify.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.alpha
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
import com.example.budgify.viewmodel.CredDebManagementViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class LoanSectionType(val title: String, val loanType: LoanType) {
    CREDITS("Credits", LoanType.CREDIT),
    DEBTS("Debts", LoanType.DEBT);

    companion object {
        fun fromLoanType(loanType: LoanType?): LoanSectionType {
            return entries.find { it.loanType == loanType } ?: DEBTS
        }
    }
}


@Composable
fun CredDebManagementScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    credDebManagementViewModel: CredDebManagementViewModel,
    initialSelectedLoanType: LoanType? = null
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.CredDebManagement.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by credDebManagementViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialSelectedLoanType) {
        initialSelectedLoanType?.let {
            credDebManagementViewModel.onSectionSelected(LoanSectionType.fromLoanType(it))
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            credDebManagementViewModel.onSnackbarMessageShown()
        }
    }

    val listState = rememberLazyListState()
    val showButton by remember {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount <= listState.layoutInfo.visibleItemsInfo.size) {
                true
            } else {
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtBottom = lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1 && listState.layoutInfo.totalItemsCount > 0
                isAtTop || isAtBottom || !listState.isScrollInProgress
            }
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
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TabRow(selectedTabIndex = uiState.selectedSection.ordinal) {
                    LoanSectionType.entries.forEach { sectionType ->
                        Tab(
                            selected = uiState.selectedSection == sectionType,
                            onClick = { credDebManagementViewModel.onSectionSelected(sectionType) },
                            text = { Text(sectionType.title) }
                        )
                    }
                }

                val explanatoryText = when (uiState.selectedSection) {
                    LoanSectionType.CREDITS -> "Here you can track all the money you lent.\nDon't forget to reclaim it ;)"
                    LoanSectionType.DEBTS -> "Here you can find all the money you borrowed.\nRemember to return it as soon as possible ;)"
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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

                Text(
                    text = "Hold on a loan to manage it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    LoansSection(
                        loans = uiState.loans,
                        listState = listState,
                        onLoanLongPressed = { credDebManagementViewModel.onLoanLongPressed(it) }
                    )
                }
            }
            AnimatedVisibility(
                visible = showButton,
                enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        navController.navigate(ScreenRoutes.CredDeb.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Back to Loans Overview")
                }
            }
        }
    }

    if (uiState.showActionChoiceDialog) {
        LoanActionChoiceDialog(
            loan = uiState.loanToAction!!,
            onDismiss = { credDebManagementViewModel.onDismissActionChoiceDialog() },
            onEditClick = { credDebManagementViewModel.onEditLoanClicked() },
            onDeleteClick = { credDebManagementViewModel.onDeleteLoanClicked() },
            onCompleteClick = { credDebManagementViewModel.onCompleteLoanClicked() }
        )
    }

    if (uiState.showEditDialog) {
        EditLoanDialog(
            loan = uiState.loanToAction!!,
            viewModel = credDebManagementViewModel,
            onDismiss = { credDebManagementViewModel.onDismissEditLoanDialog() }
        )
    }

    if (uiState.showDeleteConfirmationDialog) {
        ConfirmLoanDeleteDialog(
            loan = uiState.loanToAction!!,
            onDismiss = { credDebManagementViewModel.onDismissDeleteConfirmationDialog() },
            onConfirmDelete = { credDebManagementViewModel.onConfirmDeleteLoan() }
        )
    }

    if (uiState.showAccountSelectionForCompletionDialog) {
        AccountSelectionForCompletionDialog(
            accounts = uiState.accounts,
            hasAccounts = uiState.hasAccounts,
            title = "Account Selection",
            itemDescription = "Choose an account to create a transaction for this ${uiState.loanToAction!!.type.name.lowercase()}.",
            onDismiss = { credDebManagementViewModel.onDismissAccountSelectionDialog() },
            onAccountSelected = { credDebManagementViewModel.onAccountSelectedForCompletion(it) }
        )
    }

    if (uiState.showInsufficientBalanceDialog) {
        InsufficientBalanceDialog(
            requiredAmount = uiState.loanToAction!!.amount,
            accountInfo = uiState.insufficientBalanceAccountInfo,
            onDismiss = { credDebManagementViewModel.onDismissInsufficientBalanceDialog() }
        )
    }
}


@Composable
fun LoansSection(
    loans: List<Loan>,
    listState: LazyListState,
    onLoanLongPressed: (Loan) -> Unit
) {
    if (loans.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No loans found.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(loans, key = { it.id }) { loan ->
                LoanItem(
                    loan = loan,
                    onLoanLongPressed = onLoanLongPressed
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoanItem(
    loan: Loan,
    onLoanLongPressed: (Loan) -> Unit
) {
    val isExpired = remember(loan) {
        loan.endDate != null && loan.endDate!!.isBefore(LocalDate.now()) && !loan.completed
    }

    val backgroundColor = when {
        isExpired -> Color.Gray.copy(alpha = 0.7f)
        loan.type == LoanType.CREDIT -> Color(0xFF4CAF50).copy(alpha = if (loan.completed) 0.5f else 0.8f)
        loan.type == LoanType.DEBT -> Color(0xFFF44336).copy(alpha = if (loan.completed) 0.5f else 0.8f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val baseContentColor = MaterialTheme.colorScheme.onSurface
    val contentAlpha = if (loan.completed || isExpired) 0.7f else 1f
    val contentColor = baseContentColor.copy(alpha = contentAlpha)

    val iconImage = when {
        loan.completed -> Icons.Filled.CheckCircleOutline
        isExpired -> Icons.Filled.TimerOff
        else -> Icons.Filled.RequestQuote
    }

    val statusText = when {
        loan.completed -> if (loan.type == LoanType.DEBT) "Repaid" else "Collected"
        isExpired -> "Expired"
        else -> "Active"
    }


    val formattedAmount = String.format(java.util.Locale.US, "%.2f", loan.amount)
    val amountText = "$formattedAmount €"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { },
                onLongClick = { onLoanLongPressed(loan) }
            )
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 88.dp)
        ) {
            Text(
                text = loan.desc,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "Amount: $amountText", color = contentColor, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Start Date: ${loan.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall
            )
            loan.endDate?.let {
                Text(
                    text = "Due Date: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    color = contentColor,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isExpired) MaterialTheme.colorScheme.error else contentColor
                    )
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = statusText ?: "Loan Status",
                modifier = Modifier
                    .size(if (statusText != null) 60.dp else 80.dp)
                    .alpha(if (statusText != null) 0.7f else 0.5f),
                tint = contentColor.copy(alpha = 0.8f)
            )
            statusText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loan: Loan,
    viewModel: CredDebManagementViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(loan.desc) }
    var amount by remember { mutableStateOf(loan.amount.toString().replace('.', ',')) }
    var selectedStartDate by remember { mutableStateOf(loan.startDate) }
    var selectedEndDate by remember { mutableStateOf(loan.endDate) }
    var selectedLoanType by remember { mutableStateOf(loan.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    val loanTypes = LoanType.entries.toList()

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
                Text("Edit ${loan.type.name.lowercase()}", style = MaterialTheme.typography.titleLarge)
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
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = selectedStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        modifier = Modifier.clickable { showStartDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = selectedEndDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Not set (Optional)",
                onValueChange = {},
                label = { Text("End Date (Optional)") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select End Date",
                        modifier = Modifier.clickable { showEndDatePickerDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    loanTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedLoanType = type }
                        ) {
                            RadioButton(
                                selected = selectedLoanType == type,
                                onClick = { selectedLoanType = type })
                            Text(type.name.replaceFirstChar { it.titlecase() })
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
                        val amountDouble = amount.replace(',', '.').toDoubleOrNull()
                        if (description.isNotBlank() && amountDouble != null) {
                            val updatedLoan = loan.copy(
                                desc = description,
                                amount = amountDouble,
                                startDate = selectedStartDate,
                                endDate = selectedEndDate,
                                type = selectedLoanType,
                                completed = loan.completed
                            )
                            viewModel.onLoanUpdated(updatedLoan)
                        }
                    }) {
                    Text("Save Changes")
                }
            }
        }
    }
    if (showStartDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = { showStartDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedStartDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePickerDialog = false
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedEndDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showEndDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedEndDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePickerDialog = false
                    },
                    enabled = true
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) }
    }
}
