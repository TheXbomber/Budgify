package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: CategoryType,
    val desc: String
)

object DefaultCategories {
    val OBJECTIVES_EXP = Category(desc = "Goals (Expense)", type = CategoryType.EXPENSE) // Or your chosen type
    val OBJECTIVES_INC = Category(desc = "Goals (Income)", type = CategoryType.INCOME)
    val CREDIT_INC = Category(desc = "Credits collected", type = CategoryType.INCOME)          // Or your chosen type
    val CREDIT_EXP = Category(desc = "Credits contracted", type = CategoryType.EXPENSE)          // Or your chosen type
    val DEBT_EXP = Category(desc = "Debts repaid", type = CategoryType.EXPENSE)
    val DEBT_INC = Category(desc = "Debts contracted", type = CategoryType.INCOME)
}