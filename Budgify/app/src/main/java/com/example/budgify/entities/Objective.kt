package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class ObjectiveType {
    EXPENSE,
    INCOME
}

@Entity(
    tableName = "objectives",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Objective(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val type: ObjectiveType,
    val desc: String,
    val amount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    var completed: Boolean = false)