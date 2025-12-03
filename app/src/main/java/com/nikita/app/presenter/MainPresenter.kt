package com.nikita.app.presenter

import com.nikita.app.data.Category
import com.nikita.app.data.Expense
import com.nikita.app.data.ExpenseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter for MainActivity implementing MVP pattern
 */
class MainPresenter(
    private val view: MainContract.View,
    private val expenseDao: ExpenseDao
) : MainContract.Presenter {
    
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun saveExpense(amount: String, category: Category?, description: String, date: Long) {
        // Validate input
        if (amount.isBlank()) {
            view.showError("Please enter an amount")
            return
        }
        
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            view.showError("Please enter a valid amount")
            return
        }
        
        if (category == null) {
            view.showError("Please select a category")
            return
        }
        
        // Save to database
        presenterScope.launch {
            try {
                val expense = Expense(
                    amount = amountValue,
                    category = category,
                    description = description,
                    timestamp = date
                )
                withContext(Dispatchers.IO) {
                    expenseDao.insert(expense)
                }
                view.showSuccess()
                view.clearInput()
            } catch (e: Exception) {
                view.showError("Failed to save expense: ${e.message}")
            }
        }
    }
    
    override fun onDestroy() {
        // Cancel all coroutines when presenter is destroyed
        presenterScope.coroutineContext[Job]?.cancel()
    }
}
