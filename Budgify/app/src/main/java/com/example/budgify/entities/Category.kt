package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class CategoryType {
    EXPENSE,
    INCOME
}

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val type: CategoryType,
    val desc: String
)

object DefaultCategories {
    val OBJECTIVES_EXP = Category(id = -1, userId = "", desc = "Goals (Expense)", type = CategoryType.EXPENSE) // Or your chosen type
    val OBJECTIVES_INC = Category(id = -2, userId = "", desc = "Goals (Income)", type = CategoryType.INCOME)
    val CREDIT_INC = Category(id = -3, userId = "", desc = "Credits collected", type = CategoryType.INCOME)          // Or your chosen type
    val CREDIT_EXP = Category(id = -4, userId = "", desc = "Credits contracted", type = CategoryType.EXPENSE)          // Or your chosen type
    val DEBT_EXP = Category(id = -5, userId = "", desc = "Debts repaid", type = CategoryType.EXPENSE)
    val DEBT_INC = Category(id = -6, userId = "", desc = "Debts contracted", type = CategoryType.INCOME)
}