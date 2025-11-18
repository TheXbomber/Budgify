package com.example.budgify.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgify.entities.Account
import com.example.budgify.navigation.XButton

@Composable
fun AccountSelectionForCompletionDialog(
    accounts: List<Account>,
    hasAccounts: Boolean,
    title: String,
    itemDescription: String,
    onDismiss: () -> Unit,
    onAccountSelected: (Account) -> Unit
) {
    if (!hasAccounts && accounts.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Accounts Found") },
            text = { Text("You need to create an account first.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XButton(onDismiss = onDismiss)
                }
            },
            text = {
                if (accounts.isEmpty()) {
                    Text("Loading accounts...")
                } else {
                    Column {
                        Text(
                            itemDescription,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(accounts, key = { it.id }) { account ->
                                ListItem(
                                    headlineContent = { Text(account.title) },
                                    supportingContent = { Text("Balance: ${account.amount} €") },
                                    modifier = Modifier.clickable { onAccountSelected(account) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            },
            dismissButton = null,
            confirmButton = {}
        )
    }
}

@Composable
fun InsufficientBalanceDialog(
    requiredAmount: Double,
    accountInfo: Pair<String, Double>?,
    onDismiss: () -> Unit
) {
    if (accountInfo != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Insufficient Balance") },
            text = {
                Text(
                    "The selected account '${accountInfo.first}' does not have enough balance.\n\n" +
                            "Required: $requiredAmount €\n" +
                            "Available in '${accountInfo.first}': ${accountInfo.second} €\n\n" +
                            "Please choose another account or add funds to this one."
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AccountActionChoiceDialog(
    account: Account,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Manage '${account.title}'") },
        text = { Text("What would you like to do?") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
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
fun TransactionActionChoiceDialog(
    transactionDescription: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Manage '$transactionDescription'") },
        text = { Text("What would you like to do?") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
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
fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
