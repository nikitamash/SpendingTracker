package com.nikita.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an expense entry
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: Category,
    val description: String = "",
    val secondaryCategory: Category? = null,
    val timestamp: Long = System.currentTimeMillis()
)
