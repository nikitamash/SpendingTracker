package com.nikita.app.presenter

import com.nikita.app.data.CategoryTotal

/**
 * Contract between SummaryActivity and its presenter
 */
interface SummaryContract {
    
    interface View {
        fun showTotalSpending(total: Double)
        fun showMonthName(month: String)
        fun showCategoryBreakdown(categories: List<CategoryTotal>)
        fun showChartData(data: List<com.nikita.app.ui.BarData>)
        fun showError(message: String)
    }
    
    interface Presenter {
        fun loadSummaryData(timestamp: Long)
        fun deleteCategory(category: com.nikita.app.data.Category)
        fun onDestroy()
    }
}
