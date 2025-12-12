package com.nikita.app.presenter

import com.nikita.app.data.Category

/**
 * Contract between MainActivity and its presenter
 */
interface MainContract {
    
    interface View {
        fun showError(message: String)
        fun showSuccess()
        fun clearInput()
    }
    
    interface Presenter {
        fun saveExpense(amount: String, categories: List<Category>, description: String, date: Long)
        fun onDestroy()
    }
}
