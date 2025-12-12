package com.nikita.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikita.app.data.AppDatabase
import com.nikita.app.data.Category
import com.nikita.app.data.Expense
import com.nikita.app.data.ExpenseDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var expenseDao: ExpenseDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        expenseDao = db.expenseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val expense = Expense(
            amount = 100.0,
            category = Category.FOOD,
            description = "Test Lunch",
            timestamp = System.currentTimeMillis()
        )
        expenseDao.insert(expense)

        val byCategory = expenseDao.getAllExpenses()
        assertEquals(byCategory[0].amount, 100.0, 0.0)
        assertEquals(byCategory[0].category, Category.FOOD)
    }
    
    @Test
    @Throws(Exception::class)
    fun testTwoCategoriesIntegrity() = runBlocking {
        val expense = Expense(
            amount = 200.0,
            category = Category.RESTAURANTS,
            secondaryCategory = Category.DRINKS,
            description = "Dinner & Drinks",
            timestamp = System.currentTimeMillis()
        )
        expenseDao.insert(expense)
        
        val expenses = expenseDao.getAllExpenses()
        assertEquals(expenses[0].category, Category.RESTAURANTS)
        assertEquals(expenses[0].secondaryCategory, Category.DRINKS)
    }
}
