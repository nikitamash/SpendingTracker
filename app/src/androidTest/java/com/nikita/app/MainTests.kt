package com.nikita.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginFlow() {
        val loginPage = LoginPage()
        val username = "Nikita"
        val password = "12092002SPT"

        // Enter values
        loginPage.enterUsername(username)
        loginPage.enterPassword(password)

        // Verify values are present
        loginPage.verifyUsername(username)
        loginPage.verifyPassword(password)

        // Click login
        loginPage.clickLoginButton()
    }
}