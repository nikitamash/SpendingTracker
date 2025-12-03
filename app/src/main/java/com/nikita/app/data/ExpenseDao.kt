package com.nikita.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object for expense operations
 */
@Dao
interface ExpenseDao {
    
    @Insert
    suspend fun insert(expense: Expense)
    
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpenses(): List<Expense>
    
    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalSpending(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpending(startDate: Long, endDate: Long): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category")
    suspend fun getSpendingByCategory(): List<CategoryTotal>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE timestamp BETWEEN :startDate AND :endDate GROUP BY category")
    suspend fun getSpendingByCategory(startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("DELETE FROM expenses WHERE category = :category AND timestamp BETWEEN :startDate AND :endDate")
    suspend fun deleteExpensesByCategory(category: Category, startDate: Long, endDate: Long)
}

/**
 * Data class for category totals
 */
data class CategoryTotal(
    val category: Category,
    val total: Double
)
