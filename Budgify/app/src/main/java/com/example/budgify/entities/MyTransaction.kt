package com.example.budgify.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(
    tableName = "transactions",
    foreignKeys = [ // Define foreign key constraints
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"], // The ID column in the Account entity
            childColumns = ["accountId"], // The foreign key column in the Transaction entity
            onDelete = ForeignKey.CASCADE // Define behavior on deletion (e.g., cascade delete transactions if an account is deleted)
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"], // The ID column in the Category entity
            childColumns = ["categoryId"], // The foreign key column in the Transaction entity
            onDelete = ForeignKey.SET_NULL // Or cascade, set null, etc. Choose appropriate behavior
        )
    ]
)
data class MyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,
    val type: TransactionType,
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val categoryId: Int?
)
