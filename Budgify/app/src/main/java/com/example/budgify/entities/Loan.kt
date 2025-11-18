package com.example.budgify.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class LoanType {
    DEBT,
    CREDIT
}

@Entity(tableName = "loans")
data class Loan (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: LoanType,
    val desc: String,
    val amount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    var completed: Boolean = false
)