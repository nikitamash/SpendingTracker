package com.nikita.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.nikita.app.R

class LoginPage {

    fun enterUsername(username: String) {
        onView(withId(R.id.etUsername))
            .perform(typeText(username), closeSoftKeyboard())
    }

    fun enterPassword(password: String) {
        onView(withId(R.id.etPassword))
            .perform(typeText(password), closeSoftKeyboard())
    }

    fun clickLoginButton() {
        onView(withId(R.id.btnLogin))
            .perform(click())
    }

    fun verifyUsername(username: String) {
        onView(withId(R.id.etUsername))
            .check(matches(withText(username)))
    }

    fun verifyPassword(password: String) {
        onView(withId(R.id.etPassword))
            .check(matches(withText(password)))
    }
}
