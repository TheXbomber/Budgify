package com.example.budgify.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetails(
    @Embedded val transaction: MyTransaction, // Embed the Transaction entity
    @Relation(
        parentColumn = "accountId", // The column in the parent (Transaction) entity
        entityColumn = "id" // The column in the child (Account) entity
    )
    val account: Account, // The related Account entity

    @Relation(
        parentColumn = "categoryId", // The column in the parent (Transaction) entity
        entityColumn = "id" // The column in the child (Category) entity
    )
    val category: Category? // The related Category entity (nullable to match the foreign key)
)