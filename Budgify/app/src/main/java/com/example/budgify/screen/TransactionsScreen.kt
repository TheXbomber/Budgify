package com.example.budgify.screen

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.entities.TransactionType
import com.example.budgify.entities.TransactionWithDetails
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.TransactionsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    transactionsViewModel: TransactionsViewModel,
    authViewModel: AuthViewModel
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Transactions.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by transactionsViewModel.uiState.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactionsWithDetails.collectAsStateWithLifecycle()

    var isMapMode by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            transactionsViewModel.onSnackbarMessageShown()
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isMapMode,
                        onClick = { isMapMode = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                    SegmentedButton(
                        selected = isMapMode,
                        onClick = { isMapMode = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                    }
                }
            }

            if (isMapMode) {
                TransactionsMapView(
                    transactions = allTransactions,
                    onTransactionLongClick = { transactionsViewModel.onTransactionLongClicked(it) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        MonthlyCalendar(
                            currentMonth = uiState.currentMonth,
                            transactionDatesForCurrentMonth = uiState.transactionDatesForCurrentMonth,
                            onMonthChanged = { transactionsViewModel.onMonthChanged(it) },
                            onDaySelected = { transactionsViewModel.onDateSelected(it) }
                        )
                    }
                    item {
                        TransactionBox(
                            uiState = uiState,
                            onLongClick = { transactionsViewModel.onTransactionLongClicked(it) },
                            showSnackbar = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showTransactionActionChoiceDialog) {
        TransactionActionChoiceDialog(
            transactionDescription = uiState.transactionToAction?.description ?: "",
            onDismiss = { transactionsViewModel.onDismissActionChoiceDialog() },
            onEdit = { transactionsViewModel.onEditTransactionClicked() },
            onDelete = { transactionsViewModel.onDeleteTransactionClicked() }
        )
    }

    if (uiState.showEditTransactionDialog) {
        EditTransactionDialog(
            viewModel = transactionsViewModel,
            onDismiss = { transactionsViewModel.onDismissEditTransactionDialog() }
        )
    }

    if (uiState.showDeleteTransactionConfirmationDialog) {
        DeleteConfirmationDialog(
            title = "Confirm Deletion",
            text = "Are you sure you want to delete this transaction: \"${uiState.transactionToAction?.description}\"?",
            onDismiss = { transactionsViewModel.onDismissDeleteConfirmationDialog() },
            onConfirm = { transactionsViewModel.onConfirmDeleteTransaction() }
        )
    }

    if (uiState.showLocationPickerDialog) {
        LocationPickerDialog(
            initialLocation = if (uiState.editDialogState.latitude != null && uiState.editDialogState.longitude != null) {
                LatLng(uiState.editDialogState.latitude!!, uiState.editDialogState.longitude!!)
            } else null,
            onDismiss = { transactionsViewModel.onDismissLocationPicker() },
            onLocationSelected = { latLng ->
                transactionsViewModel.onEditDialogLocationChange(latLng.latitude, latLng.longitude)
            }
        )
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TransactionsMapView(
    transactions: List<TransactionWithDetails>,
    onTransactionLongClick: (com.example.budgify.entities.MyTransaction) -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var isMyLocationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
         isMyLocationEnabled = locationPermissionsState.allPermissionsGranted
    }

    val cameraPositionState = rememberCameraPositionState {
        // Default to Rome or some default location if no transactions
        position = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 5f) 
    }
    
    // Attempt to center on the latest transaction with location if available
    LaunchedEffect(transactions) {
        val latestTransactionWithLoc = transactions.lastOrNull { 
            it.transaction.latitude != null && it.transaction.longitude != null 
        }
        if (latestTransactionWithLoc != null) {
             val lat = latestTransactionWithLoc.transaction.latitude!!
             val lng = latestTransactionWithLoc.transaction.longitude!!
             cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 12f)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled),
        uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = true)
    ) {
        transactions.forEach { transactionWithDetails ->
            val t = transactionWithDetails.transaction
            if (t.latitude != null && t.longitude != null) {
                Marker(
                    state = MarkerState(position = LatLng(t.latitude, t.longitude)),
                    title = t.description,
                    snippet = "${t.amount}€ - ${t.date}",
                    onInfoWindowLongClick = {
                        onTransactionLongClick(t)
                    }
                )
            }
        }
    }
}


@Composable
fun MonthlyCalendar(
    currentMonth: YearMonth,
    transactionDatesForCurrentMonth: Set<LocalDate>,
    onMonthChanged: (YearMonth) -> Unit,
    onDaySelected: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val daysOfWeek = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun")

    val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // 1 per lunedì, 7 per domenica
    val startOffset = if (firstDayOfWeekValue == 0) 6 else firstDayOfWeekValue - 1

    val daysOfMonth = (1..daysInMonth).map { currentMonth.atDay(it) }
    val calendarDays = (List(startOffset) { null } + daysOfMonth)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 5.dp, 16.dp, 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val weeks = calendarDays.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { day ->
                        val isSelected = selectedDate == day
                        val isToday = day == LocalDate.now()
                        val hasTransactions = day != null && transactionDatesForCurrentMonth.contains(day)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clickable(enabled = day != null) {
                                    if (day != null) {
                                        selectedDate = day
                                        onDaySelected(day)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Surface(
                                        modifier = Modifier.padding(2.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    ) {
                                        Text(
                                            text = day.dayOfMonth.toString(),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> LocalContentColor.current
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }

                                    if (hasTransactions) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(50)
                                                )
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(7.dp))
                                    }
                                }
                            }
                        }
                    }
                    val emptySlots = 7 - week.size
                    repeat(emptySlots) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionBox(
    uiState: com.example.budgify.viewmodel.TransactionsUiState,
    onLongClick: (com.example.budgify.entities.MyTransaction) -> Unit,
    showSnackbar: (String) -> Unit
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text(
                text = if (uiState.selectedDate != null) uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) else "Latest Transactions",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Hold on a transaction to manage it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                if (uiState.transactionsForSelectedDate.isEmpty()) {
                    Text(
                        if (uiState.selectedDate != null) "No transactions for this date." else "No transactions found.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    uiState.transactionsForSelectedDate.forEach { transactionWithDetails ->
                        TransactionItem(
                            transactionWithDetails = transactionWithDetails,
                            onClick = {
                                showSnackbar("Hold to edit or delete the transaction")
                            },
                            onLongClick = onLongClick
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
    viewModel: TransactionsViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState = uiState.editDialogState
    var showDatePickerDialog by remember { mutableStateOf(false) }

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
                value = dialogState.description,
                onValueChange = { viewModel.onEditDialogDescriptionChange(it) },
                label = { Text("Description (max 30 characters)") },
                modifier = Modifier.fillMaxWidth(),
                isError = dialogState.errorMessage?.contains("Description") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = dialogState.amount,
                onValueChange = { viewModel.onEditDialogAmountChange(it) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = dialogState.errorMessage?.contains("amount") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!dialogState.isOriginalCategoryDefault) {
                CategoryDropdown(
                    categories = dialogState.availableCategories,
                    selectedCategoryId = dialogState.selectedCategoryId,
                    onCategorySelected = { viewModel.onEditDialogCategoryChange(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                val originalCategoryName = dialogState.availableCategories.firstOrNull { it.id == dialogState.transaction?.categoryId }?.desc ?: "Default Category"
                TextField(
                    value = originalCategoryName,
                    onValueChange = {},
                    label = { Text("Category (System)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            AccountDropdown(
                accounts = dialogState.availableAccounts,
                selectedAccountId = dialogState.selectedAccountId,
                onAccountSelected = { viewModel.onEditDialogAccountChange(it) },
                isError = dialogState.errorMessage?.contains("account") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = dialogState.selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
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
                modifier = Modifier.fillMaxWidth(),
                isError = dialogState.errorMessage?.contains("date") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            TransactionTypeSelector(
                selectedType = dialogState.selectedType,
                onTypeSelected = { viewModel.onEditDialogTypeChange(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location Section
            Text("Location:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (dialogState.latitude != null && dialogState.longitude != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Selected: ${String.format("%.4f", dialogState.latitude)}, ${String.format("%.4f", dialogState.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(onClick = { viewModel.onEditDialogLocationChange(null, null) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Location", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = { viewModel.onShowLocationPicker() }) {
                        Icon(Icons.Default.Map, contentDescription = "Edit Location")
                    }
                } else {
                    Text("No location set", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.onShowLocationPicker() }) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.size(4.dp))
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            dialogState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.onSaveChangesClicked() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Changes")
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dialogState.selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.onEditDialogDateChange(newDate)
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<com.example.budgify.entities.Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selectedCategory?.desc ?: "Uncategorized",
            onValueChange = {},
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Uncategorized", style = TextStyle(fontWeight = FontWeight.Bold)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text("${category.desc} (${category.type})") },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDropdown(
    accounts: List<com.example.budgify.entities.Account>,
    selectedAccountId: Int?,
    onAccountSelected: (Int?) -> Unit,
    isError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selectedAccount?.title ?: "Select Account",
            onValueChange = {},
            label = { Text("Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.title) },
                    onClick = {
                        onAccountSelected(account.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Type:")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTypeSelected(type) }
                ) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) }
                    )
                    Text(type.toString())
                }
            }
        }
    }
}
