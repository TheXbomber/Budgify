package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class LoanType {
    DEBT,
    CREDIT
}

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Loan (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val type: LoanType,
    val desc: String,
    val amount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    var completed: Boolean = false
)