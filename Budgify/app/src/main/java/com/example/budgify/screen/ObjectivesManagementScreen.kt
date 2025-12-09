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
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.ObjectivesManagementViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ObjectivesManagementScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    objectivesManagementViewModel: ObjectivesManagementViewModel,
    authViewModel: AuthViewModel
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.ObjectivesManagement.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by objectivesManagementViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            objectivesManagementViewModel.onSnackbarMessageShown()
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
        topBar = { TopBar(navController, currentRoute, authViewModel, isHomeScreen = false) },
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
                    ObjectivesManagementSection.entries.forEach { section ->
                        Tab(
                            selected = uiState.selectedSection == section,
                            onClick = { objectivesManagementViewModel.onSectionSelected(section) },
                            text = { Text(section.title) }
                        )
                    }
                }

                val explanatoryText = when (uiState.selectedSection) {
                    ObjectivesManagementSection.Active -> "Here you can find all currently active goals.\nTry to complete them before they expire!"
                    ObjectivesManagementSection.Expired -> "Here you can find all reached and/or expired goals.\nYou can still reach expired goals."
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
                    text = "Hold on a goal to manage it",
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
                    ObjectivesSection(
                        objectives = uiState.objectives,
                        listState = listState,
                        onObjectiveLongPressed = { objectivesManagementViewModel.onObjectiveLongPressed(it) }
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
                        navController.navigate(ScreenRoutes.Objectives.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Back to Stats Overview")
                }
            }
        }
    }

    if (uiState.showActionChoiceDialog) {
        ObjectiveActionChoiceDialog(
            objective = uiState.objectiveToAction!!,
            onDismiss = { objectivesManagementViewModel.onDismissActionChoiceDialog() },
            onEditClick = { objectivesManagementViewModel.onEditObjectiveClicked() },
            onDeleteClick = { objectivesManagementViewModel.onDeleteObjectiveClicked() },
            onCompleteClick = { objectivesManagementViewModel.onCompleteObjectiveClicked() }
        )
    }

    if (uiState.showEditDialog) {
        EditObjectiveDialog(
            objective = uiState.objectiveToAction!!,
            onDismiss = { objectivesManagementViewModel.onDismissEditObjectiveDialog() },
            onSave = { objectivesManagementViewModel.onObjectiveUpdated(it) }
        )
    }

    if (uiState.showDeleteConfirmationDialog) {
        DeleteObjectiveConfirmationDialog(
            objective = uiState.objectiveToAction!!,
            onDismiss = { objectivesManagementViewModel.onDismissDeleteConfirmationDialog() },
            onConfirmDelete = { objectivesManagementViewModel.onConfirmDeleteObjective() }
        )
    }

    if (uiState.showAccountSelectionForCompletionDialog) {
        AccountSelectionForCompletionDialog(
            accounts = uiState.accounts,
            hasAccounts = uiState.hasAccounts,
            title = "Select Account",
            itemDescription = "Choose an account to create a transaction for this goal",
            onDismiss = { objectivesManagementViewModel.onDismissAccountSelectionDialog() },
            onAccountSelected = { objectivesManagementViewModel.onAccountSelectedForCompletion(it) }
        )
    }

    if (uiState.showInsufficientBalanceDialog) {
        InsufficientBalanceDialog(
            requiredAmount = uiState.objectiveToAction!!.amount,
            accountInfo = uiState.insufficientBalanceAccountInfo,
            onDismiss = { objectivesManagementViewModel.onDismissInsufficientBalanceDialog() }
        )
    }
}

enum class ObjectivesManagementSection(val title: String) {
    Active("Active Goals"),
    Expired("Inactive Goals")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObjectiveItem(
    obj: Objective,
    onObjectiveLongPressed: (Objective) -> Unit
) {
    val isExpiredNotCompleted = remember(obj) {
        obj.endDate.isBefore(LocalDate.now()) && !obj.completed
    }

    val backgroundColor = when {
        isExpiredNotCompleted -> Color.Gray.copy(alpha = 0.7f)
        obj.type == ObjectiveType.INCOME -> Color(0xFF4CAF50).copy(alpha = if (obj.completed) 0.5f else 0.8f)
        obj.type == ObjectiveType.EXPENSE -> Color(0xFFF44336).copy(alpha = if (obj.completed) 0.5f else 0.8f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val baseContentColor = MaterialTheme.colorScheme.onSurface
    val contentAlpha = if (obj.completed || isExpiredNotCompleted) 0.7f else 1f
    val contentColor = baseContentColor.copy(alpha = contentAlpha)

    val iconImage = when {
        obj.completed -> Icons.Filled.CheckCircleOutline
        isExpiredNotCompleted -> Icons.Filled.TimerOff
        else -> Icons.Filled.EmojiEvents
    }

    val statusTextUnderIcon = when {
        obj.completed -> "Reached"
        isExpiredNotCompleted -> "Expired"
        else -> "Active"
    }

    val formattedAmount = String.format(java.util.Locale.US, "%.2f", obj.amount)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { },
                onLongClick = { onObjectiveLongPressed(obj) }
            )
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 88.dp)
        ) {
            Text(
                text = obj.desc,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Amount: $formattedAmount €",
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Start Date: ${obj.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Due Date: ${obj.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isExpiredNotCompleted) MaterialTheme.colorScheme.error else contentColor
                )
            )
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = statusTextUnderIcon ?: "Objective Status",
                modifier = Modifier
                    .size(if (statusTextUnderIcon != null) 60.dp else 80.dp)
                    .alpha(if (statusTextUnderIcon != null) 0.7f else 0.5f),
                tint = contentColor.copy(alpha = 0.8f)
            )
            statusTextUnderIcon?.let {
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

@Composable
fun ObjectiveActionChoiceDialog(
    objective: Objective,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    val hasExpired = objective.endDate.isBefore(LocalDate.now())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "'${objective.desc}'",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XButton(onDismiss)
            }
        },
        text = { Text("What would you like to do with this goal?") },
        confirmButton = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!objective.completed && !hasExpired)
                        TextButton(onClick = onEditClick) {
                            Text("Edit")
                        }
                    TextButton(onClick = onDeleteClick) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                if (!objective.completed) {
                    TextButton(
                        onClick = onCompleteClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Reached")
                    }
                }
            }
        },
        dismissButton = null
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditObjectiveDialog(
    objective: Objective,
    onDismiss: () -> Unit,
    onSave: (Objective) -> Unit
) {
    var description by remember { mutableStateOf(objective.desc) }
    var amount by remember { mutableStateOf(objective.amount.toString().replace('.', ',')) }
    var selectedStartDate by remember { mutableStateOf(objective.startDate) }
    var selectedEndDate by remember { mutableStateOf(objective.endDate) }
    var selectedType by remember { mutableStateOf(objective.type) }
    var showStartDatePickerDialog by remember { mutableStateOf(false) }
    var showEndDatePickerDialog by remember { mutableStateOf(false) }
    val objectiveTypes = ObjectiveType.entries.toList()

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
                Text("Edit Goal", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (max 30)") },
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
                value = selectedEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {},
                label = { Text("End Date") },
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
                    objectiveTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type })
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
                            val updatedObjective = objective.copy(
                                desc = description,
                                amount = amountDouble,
                                startDate = selectedStartDate,
                                endDate = selectedEndDate,
                                type = selectedType
                            )
                            onSave(updatedObjective)
                        }
                    }) {
                    Text("Save changes")
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
            dismissButton = {
                TextButton(onClick = { showStartDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = { showEndDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedEndDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePickerDialog = false
                    },
                    enabled = confirmEnabled
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePickerDialog = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun DeleteObjectiveConfirmationDialog(
    objective: Objective,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the goal \"${objective.desc}\"?") },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
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
fun ObjectivesSection(
    objectives: List<Objective>,
    listState: LazyListState,
    onObjectiveLongPressed: (Objective) -> Unit
) {
    if (objectives.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No goals found.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = if (listState.layoutInfo.totalItemsCount > 0) 72.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(objectives, key = { it.id }) { objective ->
                ObjectiveItem(objective, onObjectiveLongPressed)
            }
        }
    }
}
