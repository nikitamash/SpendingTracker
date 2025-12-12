package com.nikita.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nikita.app.data.AppDatabase
import com.nikita.app.data.Category
import com.nikita.app.databinding.ActivityMainBinding
import com.nikita.app.presenter.MainContract
import com.nikita.app.presenter.MainPresenter
import kotlinx.coroutines.launch

/**
 * MainActivity for adding expenses
 */
class MainActivity : AppCompatActivity(), MainContract.View {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainPresenter
    private val selectedCategories: MutableList<Category> = mutableListOf()
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize presenter
        val database = AppDatabase.getDatabase(this)
        presenter = MainPresenter(this, database.expenseDao())
        
        // Fetch exchange rates
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.nikita.app.utils.CurrencyManager.fetchRates()
        }
        
        setupCategorySelection()
        setupButtons()
        setupDateButton()
        setupCurrencyButton()
        updateDateText()
        updateCurrencyDisplay()
    }

    private fun setupCurrencyButton() {
        binding.currencyButton.setOnClickListener {
            val currencies = com.nikita.app.utils.CurrencyManager.SupportedCurrency.values()
            val items = currencies.map { it.displayName }.toTypedArray()
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setItems(items) { _, which ->
                    val selectedCurrency = currencies[which]
                    com.nikita.app.utils.CurrencyManager.saveCurrency(this, selectedCurrency)
                    updateCurrencyDisplay()
                }
                .show()
        }
    }

    private fun updateCurrencyDisplay() {
        val currency = com.nikita.app.utils.CurrencyManager.getCurrency(this)
        binding.amountInput.hint = "0 ${currency.symbol}"
    }


    
    private fun setupDateButton() {
        binding.dateButton.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            android.app.DatePickerDialog(this, R.style.Theme_SpendingTracker_Dialog_Light, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                
                // Show TimePicker after DatePicker
                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = calendar.get(java.util.Calendar.MINUTE)
                
                android.app.TimePickerDialog(this, R.style.Theme_SpendingTracker_Dialog_Light, { _, selectedHour, selectedMinute ->
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(java.util.Calendar.MINUTE, selectedMinute)
                    
                    selectedDate = calendar.timeInMillis
                    updateDateText()
                }, hour, minute, true).show()
                
            }, year, month, day).show()
        }
    }
    
    private fun updateDateText() {
        val calendar = java.util.Calendar.getInstance()
        val currentCalendar = java.util.Calendar.getInstance()
        
        calendar.timeInMillis = selectedDate
        
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val timeString = timeFormat.format(java.util.Date(selectedDate))
        
        if (calendar.get(java.util.Calendar.YEAR) == currentCalendar.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == currentCalendar.get(java.util.Calendar.DAY_OF_YEAR)) {
            binding.dateText.text = "Today $timeString"
        } else {
            binding.dateText.text = "${dateFormat.format(java.util.Date(selectedDate))} $timeString"
        }
    }
    
    private fun setupCategorySelection() {
        binding.categoryFood.setOnClickListener {
            selectCategory(Category.FOOD, binding.categoryFood)
        }
        
        binding.categoryRestaurants.setOnClickListener {
            selectCategory(Category.RESTAURANTS, binding.categoryRestaurants)
        }
        
        binding.categoryDrinks.setOnClickListener {
            selectCategory(Category.DRINKS, binding.categoryDrinks)
        }
        
        binding.categoryShopping.setOnClickListener {
            selectCategory(Category.SHOPPING, binding.categoryShopping)
        }
        
        binding.categoryTransport.setOnClickListener {
            selectCategory(Category.TRANSPORT, binding.categoryTransport)
        }
        
        binding.categoryUtilities.setOnClickListener {
            selectCategory(Category.UTILITIES, binding.categoryUtilities)
        }

        binding.categoryBankCard.setOnClickListener {
            selectCategory(Category.BANK_CARD, binding.categoryBankCard)
        }
        
        binding.categoryEntertainment.setOnClickListener {
            selectCategory(Category.ENTERTAINMENT, binding.categoryEntertainment)
        }
    }
    
    private fun getCategoryView(category: Category): android.view.View {
        return when (category) {
            Category.FOOD -> binding.categoryFood
            Category.RESTAURANTS -> binding.categoryRestaurants
            Category.DRINKS -> binding.categoryDrinks
            Category.SHOPPING -> binding.categoryShopping
            Category.TRANSPORT -> binding.categoryTransport
            Category.UTILITIES -> binding.categoryUtilities
            Category.BANK_CARD -> binding.categoryBankCard
            Category.ENTERTAINMENT -> binding.categoryEntertainment
        }
    }

    private fun selectCategory(category: Category, view: android.view.View) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
            view.isSelected = false
        } else {
            if (selectedCategories.size >= 2) {
                // Remove the first selected category to handle max 2 selection
                val removed = selectedCategories.removeAt(0)
                getCategoryView(removed).isSelected = false 
            }
            selectedCategories.add(category)
            view.isSelected = true
        }
    }
    
    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            val amountStr = binding.amountInput.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDouble()
                val currentCurrency = com.nikita.app.utils.CurrencyManager.getCurrency(this)
                val amountInLek = com.nikita.app.utils.CurrencyManager.convertToLek(amount, currentCurrency)
                
                val description = binding.descriptionInput.text.toString()
                presenter.saveExpense(amountInLek.toString(), selectedCategories, description, selectedDate)
            } else {
                showError("Please enter an amount")
            }
        }
        
        binding.viewSummaryButton.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun showSuccess() {
        Toast.makeText(this, getString(R.string.success_saved), Toast.LENGTH_SHORT).show()
    }
    
    override fun clearInput() {
        binding.amountInput.text?.clear()
        binding.descriptionInput.text?.clear()
        selectedCategories.forEach { category ->
             getCategoryView(category).isSelected = false
        }
        selectedCategories.clear()
        selectedDate = System.currentTimeMillis()
        updateDateText()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}
