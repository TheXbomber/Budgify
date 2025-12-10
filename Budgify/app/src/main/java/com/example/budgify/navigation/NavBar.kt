package com.example.budgify.navigation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.budgify.R
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.applicationlogic.ReceiptScanRepository
import com.example.budgify.auth.AuthViewModel
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import com.example.budgify.routes.ARG_INITIAL_LOAN_TYPE
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.screen.AddCategoryDialog
import com.example.budgify.screen.items
import com.example.budgify.screen.smallTextStyle
import com.example.budgify.utils.parseTransactionDate
import com.example.budgify.utils.processImageForReceipt
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, currentRoute: String, authViewModel: AuthViewModel, isHomeScreen: Boolean) {
    val title = when (currentRoute) {
        ScreenRoutes.Home.route -> "Dashboard"
        "objectives_screen" -> "Goals and Stats"
        "objectives_management_screen" -> "Manage Goals"
        "settings_screen" -> "Settings"
        "transactions_screen" -> "Transactions"
        "cred_deb_screen" -> "Loans"
        "cred_deb_management_screen/{$ARG_INITIAL_LOAN_TYPE}?" -> "Manage Loans"
        "categories_screen" -> "Categories"
        else -> ""
    }

    var showLogoutDialog by remember { mutableStateOf(false) } // State for logout dialog

    CenterAlignedTopAppBar(
        title = { Text(title, fontSize = 30.sp) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (isHomeScreen) {
                IconButton(onClick = { showLogoutDialog = true }) { // Show dialog on click
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                IconButton(onClick = {
                    navController.navigate(ScreenRoutes.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate(ScreenRoutes.Settings.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                Icon( Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface)
            }

        }
    )

    if (showLogoutDialog) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    showLogoutDialog = false
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error) // Added color
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { // Changed to TextButton
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BottomBar(
    navController: NavController,
    viewModel: FinanceViewModel,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(bottom = 0.dp)
    ) {
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            items.forEach { screen ->
                val iconModifier = if (screen == ScreenRoutes.Adding) {
                    Modifier.size(45.dp)
                } else {
                    Modifier.size(30.dp)
                }
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = screen.icon),
                            contentDescription = null,
                            modifier = iconModifier
                        )
                    },
                    label = {
                        if (screen != ScreenRoutes.Adding) {
                            Text(screen.title, style = smallTextStyle)
                        }
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        if (screen == ScreenRoutes.Adding) {
                            showDialog = true
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showDialog = false }
                        ),
                    contentAlignment = Alignment.BottomCenter

                ) {
                    Column(
                        modifier = Modifier
                            .padding(0.dp, 0.dp, 0.dp, 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                viewModel.onShowAddTransactionDialog()
                                showDialog = false
                            },
                            modifier = Modifier
                                .padding(0.dp)
                                .width(150.dp)
                                .height(50.dp)
                        ) {
                            Text("Transaction")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.onShowAddObjectiveDialog()
                                showDialog = false
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .padding(0.dp)
                        ) {
                            Text("Goal")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.onShowAddLoanDialog()
                                showDialog = false
                            },
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .padding(0.dp)
                        ) {
                            Text("Loan")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onTransactionAdded: (MyTransaction) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categoriesForDropdown by viewModel.categoriesForTransactionDialog.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedType by remember { mutableStateOf<TransactionType>(TransactionType.EXPENSE) }
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }
    val transactionTypes = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    val userId by viewModel.userId.collectAsStateWithLifecycle()

    // Receipt Scanning States
    var isLoadingReceiptScan by remember { mutableStateOf(false) }
    var showScanErrorDialog by remember { mutableStateOf(false) }
    var scanErrorMessage by remember { mutableStateOf("") }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) } // For camera capture
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var permissionDeniedMessage by remember { mutableStateOf("") }

    // Flags to track if a launch is pending after permission grant
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    var pendingGalleryLaunch by remember { mutableStateOf(false) }

    val receiptScanRepository = remember { ReceiptScanRepository() }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
    val storagePermissionState = rememberPermissionState(storagePermission)

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            scope.launch {
                processImageForReceipt(
                    imageUri = tempImageUri!!,
                    context = context,
                    receiptScanRepository = receiptScanRepository,
                    onLoading = { isLoadingReceiptScan = it },
                    onSuccess = { parsedTransaction ->
                        description = parsedTransaction.description
                        amount = parsedTransaction.amount.toString()
                        selectedDate = parseTransactionDate(parsedTransaction.date)
                    },
                    onError = { message ->
                        scanErrorMessage = message
                        showScanErrorDialog = true
                    }
                )
            }
        }
        tempImageUri = null // Clear temp URI after processing
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                processImageForReceipt(
                    imageUri = uri,
                    context = context,
                    receiptScanRepository = receiptScanRepository,
                    onLoading = { isLoadingReceiptScan = it },
                    onSuccess = { parsedTransaction ->
                        description = parsedTransaction.description
                        amount = parsedTransaction.amount.toString()
                        selectedDate = parseTransactionDate(parsedTransaction.date)
                    },
                    onError = { message ->
                        scanErrorMessage = message
                        showScanErrorDialog = true
                    }
                )
            }
        }
    }

    // LaunchedEffect to react to camera permission status changes
    LaunchedEffect(cameraPermissionState.status) {
        if (pendingCameraLaunch && cameraPermissionState.status.isGranted) {
            pendingCameraLaunch = false
            val photoFile = File(context.cacheDir, "receipt_image.jpg")
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(tempImageUri!!)
        }
    }

    // LaunchedEffect to react to storage permission status changes
    LaunchedEffect(storagePermissionState.status) {
        if (pendingGalleryLaunch && storagePermissionState.status.isGranted) {
            pendingGalleryLaunch = false
            galleryLauncher.launch("image/*")
        }
    }


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
                    "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                )
                XButton(onDismiss)
            }
            Text(
                "Add a transaction to record an expense or income in your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val photoFile = File(context.cacheDir, "receipt_image.jpg")
                        tempImageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        cameraLauncher.launch(tempImageUri!!)
                    } else if (cameraPermissionState.status.shouldShowRationale) {
                        permissionDeniedMessage = "Camera permission is required to scan receipts using the camera. Please grant it."
                        showPermissionRationaleDialog = true
                    } else {
                        pendingCameraLaunch = true // Set flag, then request
                        cameraPermissionState.launchPermissionRequest()
                    }
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Scan with Camera", modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = {
                    if (storagePermissionState.status.isGranted) {
                        galleryLauncher.launch("image/*")
                    } else if (storagePermissionState.status.shouldShowRationale) {
                        permissionDeniedMessage = "Storage permission is required to select receipts from your gallery. Please grant it."
                        showPermissionRationaleDialog = true
                    } else {
                        pendingGalleryLaunch = true // Set flag, then request
                        storagePermissionState.launchPermissionRequest()
                    }
                }) {
                    Icon(Icons.Filled.Image, contentDescription = "Select from Gallery", modifier = Modifier.size(36.dp))
                }
            }
            if (isLoadingReceiptScan) {
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally))
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { newValue ->
                    var cleanedValue = newValue.replace("\n", "").replace("\t", "")
                    cleanedValue = cleanedValue.replace(Regex("\\s+"), " ")
                    if (cleanedValue.length <= 30) {
                        description = cleanedValue
                        descriptionError = null
                    } else {
                        description = cleanedValue.take(30)
                        descriptionError = "Max 30 characters allowed."
                    }
                },
                label = { Text("Description (max 30 characters)") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError != null,
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

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selectedCategory?.desc ?: "Uncategorized",
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Uncategorized", style = TextStyle(fontWeight = FontWeight.Bold), fontStyle = FontStyle.Italic) },
                        onClick = {
                            selectedCategoryId = null
                            selectedCategory = null
                            categoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                    categoriesForDropdown.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.desc} (${category.type})") },
                            onClick = {
                                selectedCategoryId = category.id
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Add New Category...", style = TextStyle(fontWeight = FontWeight.Bold), fontStyle = FontStyle.Italic) },
                        onClick = {
                            categoryExpanded = false
                            showAddCategoryDialog = true
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = selectedAccount?.title ?: "Select Account",
                        onValueChange = {},
                        label = { Text("Account") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.title) },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No accounts available. Please create an account first to save transactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

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

            if (selectedCategory == null) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Type:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        transactionTypes.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedType = type }
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                Text(type.toString())
                            }
                        }
                    }
                }
            } else {
                selectedType = when (selectedCategory!!.type) {
                    CategoryType.EXPENSE -> TransactionType.EXPENSE
                    CategoryType.INCOME -> TransactionType.INCOME
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedDate != null && selectedAccountId != null,
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull()
                        if (description.isNotBlank() && amountDouble != null && selectedDate != null && selectedAccountId != null && userId != null) {
                            val newTransaction = MyTransaction(
                                userId = userId!!,
                                accountId = selectedAccountId!!,
                                type = selectedType,
                                date = selectedDate!!,
                                description = description,
                                amount = amountDouble,
                                categoryId = selectedCategoryId
                            )
                            viewModel.addTransaction(newTransaction)
                            onTransactionAdded(newTransaction)
                        }
                    }) {
                    Text("Add")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialDateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
            },
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
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            initialType = null,
            onCategoryAdd = { description, type ->
                if (userId != null) {
                    val newCategory = Category(userId = userId!!, type = type, desc = description)
                    viewModel.addCategory(newCategory) {
                        selectedCategoryId = it.id
                        selectedCategory = it
                        showAddCategoryDialog = false
                    }
                }
            }
        )
    }

    if (showScanErrorDialog) {
        AlertDialog(
            onDismissRequest = { showScanErrorDialog = false },
            title = { Text("Receipt Scan Error") },
            text = { Text(scanErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showScanErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Permissions Required") },
            text = { Text(permissionDeniedMessage) },
            confirmButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectiveDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel,
    onObjectiveAdded: (Objective) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedType by remember { mutableStateOf(ObjectiveType.EXPENSE) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val objectiveTypes = ObjectiveType.entries.toList()
    val userId by viewModel.userId.collectAsStateWithLifecycle()

    var descriptionError by remember { mutableStateOf<String?>(null) }

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
                Text("Add Goal", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }
            Text(
                "Add a goal to record a sum of money you want to gain or spend in the future.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { newValue ->
                    var cleanedValue = newValue.replace("\n", "").replace("\t", "")
                    cleanedValue = cleanedValue.replace(Regex("\\s+"), " ")
                    if (cleanedValue.length <= 30) {
                        description = cleanedValue
                        descriptionError = null
                    } else {
                        description = cleanedValue.take(30)
                        descriptionError = "Max 30 characters allowed."
                    }
                },
                label = { Text("Description (max 30 characters)") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError != null,
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

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    objectiveTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                selectedType = type
                            }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedDate != null,
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull()
                        if (description.isNotBlank() && amountDouble != null && selectedDate != null && userId != null) {
                            val newObjective = Objective(
                                userId = userId!!,
                                id = 0,
                                type = selectedType,
                                desc = description,
                                amount = amountDouble,
                                startDate = LocalDate.now(),
                                endDate = selectedDate!!
                            )

                            viewModel.addObjective(newObjective)
                            onObjectiveAdded(newObjective)
                        }
                    }) {
                    Text("Add")
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val initialDateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
            },
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
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                    }
                ) {
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
fun AddLoanDialog(
    onDismiss: () -> Unit,
    viewModel: FinanceViewModel,
    onLoanAdded: (Loan) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedType by remember { mutableStateOf(LoanType.DEBT) }
    val loanTypes = LoanType.entries.toList()
    val userId by viewModel.userId.collectAsStateWithLifecycle()

    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId }
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }

    var descriptionError by remember { mutableStateOf<String?>(null) }

    fun triggerError(message: String) {
        errorMessage = message
        showErrorDialog = true
    }

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
                Text("Add Loan", style = MaterialTheme.typography.titleLarge)
                XButton(onDismiss)
            }
            Text(
                "Add a loan to record a credit or a debit you have contracted to your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = description,
                onValueChange = { newValue ->
                    var cleanedValue = newValue.replace("\n", "").replace("\t", "")
                    cleanedValue = cleanedValue.replace(Regex("\\s+"), " ")
                    if (cleanedValue.length <= 30) {
                        description = cleanedValue
                        descriptionError = null
                    } else {
                        description = cleanedValue.take(30)
                        descriptionError = "Max 30 characters allowed."
                    }
                },
                label = { Text("Description (max 30 characters)") },
                modifier = Modifier.fillMaxWidth(),
                isError = descriptionError != null || (description.isBlank() && showErrorDialog),
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = selectedAccount?.title ?: "Select Account",
                        onValueChange = {},
                        label = { Text("Account") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        isError = selectedAccountId == null && showErrorDialog
                    )

                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.title} (${account.amount}€)") },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No accounts available. Please create an account first to associate with this loan's transaction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = selectedStartDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text("Start Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        modifier = Modifier.clickable {
                            datePickerTarget = "START"
                            showDatePickerDialog = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = selectedStartDate == null && showErrorDialog
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
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear End Date"
                                )
                            }
                        }
                        IconButton(onClick = {
                            datePickerTarget = "END"
                            showDatePickerDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select End Date"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(type.name)
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
                    enabled = description.isNotBlank() && amount.isNotBlank() && selectedStartDate != null && selectedAccountId != null && accounts.isNotEmpty(),
                    onClick = {
                        if (description.isBlank()) {
                            triggerError("Description cannot be empty.")
                            return@Button
                        }

                        val sanitizedAmount = amount.replace(',', '.')
                        val amountDouble = sanitizedAmount.toDoubleOrNull()

                        if (amountDouble == null || amountDouble <= 0) {
                            triggerError("Please enter a valid amount greater than zero.")
                            return@Button
                        }

                        if (selectedAccountId == null) {
                            triggerError("Please select an account to associate with this loan's transaction.")
                            return@Button
                        }

                        val currentAccount = selectedAccount!!


                        if (selectedType == LoanType.CREDIT && amountDouble > currentAccount.amount) {
                            errorMessage = "The selected account '${currentAccount.title}' does not have enough balance to grant this credit.\n\n" +
                                    "Required: $amountDouble €\n" +
                                    "Available in '${currentAccount.title}': ${currentAccount.amount} €\n\n" +
                                    "Please choose another account or add funds to this one."
                            showInsufficientBalanceDialog = true
                            return@Button
                        }

                        if (selectedStartDate == null) {
                            triggerError("Please select a start date.")
                            return@Button
                        }

                        if (selectedEndDate != null && selectedStartDate != null && selectedEndDate!!.isBefore(selectedStartDate!!)) {
                            triggerError("End date cannot be before the start date.")
                            return@Button
                        }
                        if (userId != null) {
                            val newLoan = Loan(
                                userId = userId!!,
                                desc = description,
                                amount = amountDouble,
                                type = selectedType,
                                startDate = selectedStartDate!!,
                                endDate = selectedEndDate
                            )
                            viewModel.addLoan(newLoan, selectedAccountId!!)
                            onLoanAdded(newLoan)
                            onDismiss()
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }

    if (showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = {
                showInsufficientBalanceDialog = false
            },
            title = { Text("Insufficient Balance") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showInsufficientBalanceDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDatePickerDialog) {
        val initialDateForPicker = when (datePickerTarget) {
            "START" -> selectedStartDate ?: LocalDate.now()
            "END" -> selectedEndDate ?: selectedStartDate ?: LocalDate.now()
            else -> LocalDate.now()
        }
        val initialDateMillis = initialDateForPicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
                datePickerTarget = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerTarget = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Validation Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun XButton(onDismiss: () -> Unit) {
    IconButton(
        onClick = onDismiss,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Icon(Icons.Filled.Close, contentDescription = "Close")
    }
}