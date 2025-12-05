package com.example.budgify.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.budgify.applicationlogic.FinanceViewModel
import com.example.budgify.entities.Category
import com.example.budgify.entities.CategoryType
import com.example.budgify.navigation.BottomBar
import com.example.budgify.navigation.TopBar
import com.example.budgify.navigation.XButton
import com.example.budgify.routes.ScreenRoutes
import com.example.budgify.viewmodel.CategoriesViewModel
import kotlinx.coroutines.launch

// Define the possible sections for categories
enum class CategoriesTab(val title: String) {
    Expenses("Expense"),
    Income("Income")
}

@Composable
fun CategoriesScreen(
    navController: NavController,
    viewModel: FinanceViewModel,
    categoriesViewModel: CategoriesViewModel
) {
    val currentRoute by remember { mutableStateOf(ScreenRoutes.Categories.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by categoriesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            categoriesViewModel.onSnackbarMessageShown()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                CategoriesTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { categoriesViewModel.onTabSelected(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            val explanatoryText = when (uiState.selectedTab) {
                CategoriesTab.Expenses -> "Here you can find all the expense categories you have created."
                CategoriesTab.Income -> "Here you can find all the income categories you have created."
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
                        style = MaterialTheme.typography.bodyMedium, // Puoi scegliere lo stile che preferisci
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Aggiungi padding per spaziatura
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = "Hold on a category to manage it",
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
                    .padding(horizontal = 16.dp)
            ) {
                when (uiState.selectedTab) {
                    CategoriesTab.Expenses -> {
                        CategoryGridSection(
                            categories = uiState.expenseCategories,
                            categoryType = CategoryType.EXPENSE,
                            backgroundColor = Color(0xFFF44336), // Red
                            onAddClick = { categoriesViewModel.onAddCategoryClicked() },
                            onCategoryClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Hold to choose an action for the category")
                                }
                            },
                            onCategoryLongClick = { category ->
                                categoriesViewModel.onCategoryLongClicked(category)
                            },
                        )
                    }
                    CategoriesTab.Income -> {
                        CategoryGridSection(
                            categories = uiState.incomeCategories,
                            categoryType = CategoryType.INCOME,
                            backgroundColor = Color(0xFF4CAF50), // Green
                            onAddClick = { categoriesViewModel.onAddCategoryClicked() },
                            onCategoryClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Hold to choose an action for the category")
                                }
                            },
                            onCategoryLongClick = { category ->
                                categoriesViewModel.onCategoryLongClicked(category)
                            },
                        )
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    if (uiState.showAddDialog) {
        AddCategoryDialog(
            initialType = when (uiState.selectedTab) {
                CategoriesTab.Expenses -> CategoryType.EXPENSE
                CategoriesTab.Income -> CategoryType.INCOME
            },
            onDismiss = { categoriesViewModel.onDismissAddCategoryDialog() },
            onCategoryAdd = { description, type ->
                categoriesViewModel.addCategory(description, type)
            }
        )
    }

    uiState.categoryToAction?.let { category ->
        if (uiState.showCategoryActionChoiceDialog) {
            CategoryActionChoiceDialog(
                category = category,
                onDismiss = { categoriesViewModel.onDismissCategoryActionChoiceDialog() },
                onEditClick = { categoriesViewModel.onEditCategoryClicked() },
                onDeleteClick = { categoriesViewModel.onDeleteCategoryClicked() }
            )
        }
        if (uiState.showEditCategoryDialog) {
            EditCategoryDialog(
                category = category,
                onDismiss = { categoriesViewModel.onDismissEditCategoryDialog() },
                onCategoryUpdate = { description ->
                    categoriesViewModel.updateCategory(description)
                }
            )
        }
    }

    uiState.showDeleteConfirmDialog?.let { categoryToDelete ->
        DeleteCategoryConfirmationDialog(
            category = categoryToDelete,
            onDismiss = { categoriesViewModel.onDismissDeleteCategoryDialog() },
            onDeleteConfirmed = { categoriesViewModel.deleteCategory(it) }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    category: Category,
    backgroundColor: Color,
    onClick: (Category) -> Unit,
    onLongClick: (Category) -> Unit // Callback for long click
) {

    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 1f)
    val image = Icons.Filled.Sell

    Box(
        modifier = Modifier
            .width(150.dp) // Fixed width for better alignment in grid
            .height(80.dp) // Fixed height
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .background(backgroundColor.copy(alpha = 0.8f))
            .combinedClickable( // Handle long click
                onClick = { onClick(category) },
                onLongClick = { onLongClick(category) } // Trigger the long click callback
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = image, // Trophy icon
            contentDescription = "Category",
            modifier = Modifier
                .align(Alignment.CenterEnd) // Align to the center-end of the Box
                .size(50.dp) // Adjust size as needed
                .padding(end = 8.dp) // Some padding from the edge
                .alpha(0.2f), // Set transparency (0.0f is fully transparent, 1.0f is fully opaque)
            tint = contentColor.copy(alpha = 0.7f) // Optional: tint to match content color with more alpha
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.desc,
                textAlign = TextAlign.Center,
                color = contentColor // Make text visible on colored background
            )
        }
    }
}

@Composable
fun AddCategoryButton(
    categoryType: CategoryType, // Keep type for context if needed inside
    onClick: () -> Unit // Use a simple onClick lambda
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick), // Call the provided lambda on click
        contentAlignment = Alignment.Center // Center the content (Icon)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Category",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CategoryGridSection(
    categories: List<Category>,
    categoryType: CategoryType,
    backgroundColor: Color,
    onAddClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit, // Receive long click callback
) {
    if (categories.isEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Or GridCells.Adaptive(minSize = 120.dp)
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 8.dp
            ), // Adjust padding as needed
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add the "Add Category" button as the first item
            item {
                AddCategoryButton(categoryType = categoryType, onClick = onAddClick)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .padding(16.dp), // Add some padding
            contentAlignment = Alignment.Center // Center the text
        ) {
            Text(
                text = "No categories found for ${categoryType.name.lowercase()}.\nTap the '+' button to add a new one!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Make it slightly less prominent
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Or GridCells.Adaptive(minSize = 120.dp)
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 8.dp
            ), // Adjust padding as needed
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add the "Add Category" button as the first item
            item {
                AddCategoryButton(categoryType = categoryType, onClick = onAddClick)
            }

            // Display the list of categories
            items(categories, key = { it.id }) { category -> // Add a key for performance
                CategoryItem(
                    category = category,
                    backgroundColor = backgroundColor,
                    onClick = onCategoryClick,
                    onLongClick = onCategoryLongClick // Pass callback
                )
            }
        }
    }
}


// Dialog Composable Functions

@Composable
fun AddCategoryDialog(
    initialType: CategoryType?,
    onDismiss: () -> Unit,
    onCategoryAdd: (String, CategoryType) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType ?: CategoryType.EXPENSE) }
    val categoryTypes = CategoryType.entries.toList()
    var showError by remember { mutableStateOf(false) } // Stato per mostrare errore lunghezza

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Category",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    XButton(onDismiss)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { newValue ->
                        var cleanedValue = newValue.replace("\n", "").replace("\t", "")
                        cleanedValue = cleanedValue.replace(Regex("\\s+"), " ")
                        if (cleanedValue.length <= 20) {
                            description = cleanedValue
                            showError = false
                        } else {
                            description = cleanedValue.take(20)
                            showError = true
                        }
                    },
                    label = { Text("Category Name (max 20)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("Max 20 characters allowed.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                if (initialType == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Type:")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            categoryTypes.forEach { type ->
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            val finalDescription = description.trim()
                            if (finalDescription.isNotBlank()) {
                                onCategoryAdd(finalDescription, selectedType)
                            }
                        },
                        enabled = description.trim().isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryActionChoiceDialog(
    category: Category,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("'${category.desc}'")
                XButton(onDismiss)
            }
        },
        text = { Text("What would you like to do with this category?") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onEditClick) {
                    Text("Edit")
                }
                TextButton(onClick = onDeleteClick) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = null
    )
}


@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onCategoryUpdate: (String) -> Unit
) {
    var description by remember { mutableStateOf(category.desc) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Category",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    XButton(onDismiss)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { newValue ->
                        var cleanedValue = newValue.replace("\n", "").replace("\t", "")
                        cleanedValue = cleanedValue.replace(Regex("\\s+"), " ")
                        if (cleanedValue.length <= 20) {
                            description = cleanedValue
                            showError = false
                        } else {
                            description = cleanedValue.take(20)
                            showError = true
                        }
                    },
                    label = { Text("Category Name (max 20)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("Max 20 characters allowed.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val finalDescription = description.trim()
                            if (finalDescription.isNotBlank()) {
                                onCategoryUpdate(finalDescription)
                            }
                        },
                        enabled = description.trim().isNotBlank() && description.trim() != category.desc
                    ) {
                        Text("Save changes")
                    }
                }
            }
        }
    }
}


@Composable
fun DeleteCategoryConfirmationDialog(
    category: Category,
    onDismiss: () -> Unit,
    onDeleteConfirmed: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete the category \"${category.desc}\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteConfirmed(category)
                }
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
