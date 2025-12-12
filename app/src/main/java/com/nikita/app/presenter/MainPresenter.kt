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
    
    override fun saveExpense(amount: String, categories: List<Category>, description: String, date: Long) {
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
        
        if (categories.isEmpty()) {
            view.showError("Please select at least one category")
            return
        }

        if (categories.size > 2) {
             view.showError("Please select at most two categories")
             return
        }
        
        // Save to database
        presenterScope.launch {
            try {
                val expense = Expense(
                    amount = amountValue,
                    category = categories[0],
                    secondaryCategory = if (categories.size > 1) categories[1] else null,
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
