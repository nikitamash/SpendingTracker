package com.nikita.app.presenter

import com.nikita.app.data.Category
import com.nikita.app.data.ExpenseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter for SummaryActivity implementing MVP pattern
 */
class SummaryPresenter(
    private val view: SummaryContract.View,
    private val expenseDao: ExpenseDao
) : SummaryContract.Presenter {
    
    private val presenterScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentMonthTimestamp: Long = System.currentTimeMillis()
    
    override fun loadSummaryData(timestamp: Long) {
        currentMonthTimestamp = timestamp
        presenterScope.launch {
            try {
                // Calculate start and end of selected month
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = timestamp
                
                // Set to first day of month at 00:00:00
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                
                // Show current month name
                val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                view.showMonthName(monthFormat.format(java.util.Date(startDate)))
                
                // Set to last day of month at 23:59:59
                calendar.add(java.util.Calendar.MONTH, 1)
                calendar.add(java.util.Calendar.MILLISECOND, -1)
                val endDate = calendar.timeInMillis
                
                // Load total spending for current month
                val total = withContext(Dispatchers.IO) {
                    expenseDao.getTotalSpending(startDate, endDate) ?: 0.0
                }
                view.showTotalSpending(total)
                
                // Load category breakdown for current month
                // Load all expenses for current month manually to handle split categories
                val expenses = withContext(Dispatchers.IO) {
                    expenseDao.getExpenses(startDate, endDate)
                }
                
                val totalsMap = mutableMapOf<Category, Double>()
                
                expenses.forEach { expense ->
                    if (expense.secondaryCategory != null) {
                        val splitAmount = expense.amount / 2
                        val cat1 = expense.category
                        val cat2 = expense.secondaryCategory
                        totalsMap[cat1] = (totalsMap[cat1] ?: 0.0) + splitAmount
                        totalsMap[cat2] = (totalsMap[cat2] ?: 0.0) + splitAmount
                    } else {
                        val cat = expense.category
                        totalsMap[cat] = (totalsMap[cat] ?: 0.0) + expense.amount
                    }
                }
                
                // Find max category
                val maxCategory = totalsMap.maxByOrNull { it.value }?.key

                // Create a list of all categories with their totals (including $0)
                val allCategoryTotals = Category.values().map { category ->
                    com.nikita.app.data.CategoryTotal(
                        category = category,
                        total = totalsMap[category] ?: 0.0
                    )
                }
                
                // Filter out categories with zero totals
                val nonZeroCategories = allCategoryTotals.filter { it.total > 0.0 }
                view.showCategoryBreakdown(nonZeroCategories)

                val chartData = Category.values().map { category ->
                    com.nikita.app.ui.BarData(
                        label = category.iconRes,
                        value = totalsMap[category]?.toFloat() ?: 0f,
                        iconDrawable = category.iconDrawable,
                        isSelected = category == maxCategory
                    )
                }
                view.showChartData(chartData)
                
            } catch (e: Exception) {
                view.showError("Failed to load data: ${e.message}")
            }
        }
    }

    override fun deleteCategory(category: com.nikita.app.data.Category) {
        presenterScope.launch {
            try {
                // Calculate start and end of current month
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = currentMonthTimestamp
                
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                
                calendar.add(java.util.Calendar.MONTH, 1)
                calendar.add(java.util.Calendar.MILLISECOND, -1)
                val endDate = calendar.timeInMillis
                
                // Delete expenses for this category
                withContext(Dispatchers.IO) {
                    expenseDao.deleteExpensesByCategory(category, startDate, endDate)
                }
                
                // Reload data
                loadSummaryData(currentMonthTimestamp)
            } catch (e: Exception) {
                view.showError("Failed to delete: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        // Cancel all coroutines when presenter is destroyed
        presenterScope.coroutineContext[Job]?.cancel()
    }
}
