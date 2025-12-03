package com.nikita.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikita.app.data.AppDatabase
import com.nikita.app.data.CategoryTotal
import com.nikita.app.databinding.ActivitySummaryBinding
import com.nikita.app.presenter.SummaryContract
import com.nikita.app.presenter.SummaryPresenter
import com.nikita.app.ui.CategorySummaryAdapter
import java.text.NumberFormat
import java.util.Locale

/**
 * SummaryActivity for viewing expense totals and breakdowns
 */
class SummaryActivity : AppCompatActivity(), SummaryContract.View {
    
    private lateinit var binding: ActivitySummaryBinding
    private lateinit var presenter: SummaryPresenter
    private lateinit var categoryAdapter: CategorySummaryAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("sq", "AL"))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize presenter
        val database = AppDatabase.getDatabase(this)
        presenter = SummaryPresenter(this, database.expenseDao())
        
        setupRecyclerView()
        setupBackButton()
        setupLogoutButton()
        setupMonthSelector()
        presenter.loadSummaryData(System.currentTimeMillis())
    }
    
    private fun setupMonthSelector() {
        binding.calendarButton.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            android.app.DatePickerDialog(this, R.style.Theme_SpendingTracker_Dialog_Light, { _, selectedYear, selectedMonth, _ ->
                calendar.set(java.util.Calendar.YEAR, selectedYear)
                calendar.set(java.util.Calendar.MONTH, selectedMonth)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1) // Default to 1st of month
                
                presenter.loadSummaryData(calendar.timeInMillis)
            }, year, month, day).show()
        }
    }
    
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            val intent = Intent(this, Lo12092002LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        categoryAdapter = CategorySummaryAdapter { category ->
            presenter.deleteCategory(category)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SummaryActivity)
            adapter = categoryAdapter
        }
    }
    
    override fun showTotalSpending(total: Double) {
        binding.totalSpendingText.text = currencyFormat.format(total)
    }
    
    override fun showMonthName(month: String) {
        binding.monthLabel.text = month
    }
    
    override fun showChartData(data: List<com.nikita.app.ui.BarData>) {
        binding.chartView.setData(data)
    }
    
    override fun showCategoryBreakdown(categories: List<CategoryTotal>) {
        categoryAdapter.submitList(categories)
    }
    
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
