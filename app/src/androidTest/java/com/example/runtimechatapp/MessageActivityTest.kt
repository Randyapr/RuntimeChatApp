package com.example.runtimechatapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.runtimechatapp.ui.MessageActivity
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MessageActivity::class.java)

    @Test
    fun testSendMessage() {
        // Inputkan teks pesan
        val testMessage = "Assalamu'alaikum, ini merupakan pesan uji"
        onView(withId(R.id.messageEditText))
            .perform(typeText(testMessage), closeSoftKeyboard())

        // Klik tombol Kirim
        onView(withId(R.id.sendButton))
            .perform(click())
    }

    @Test
    fun testEmptyMessageShouldNotSend() {
        // Pastikan input kosong
        onView(withId(R.id.messageEditText))
            .perform(clearText())

        // Klik tombol Kirim
        onView(withId(R.id.sendButton))
            .perform(click())

        // Verifikasi pesan tidak terkirim
        // Misalnya dengan memeriksa jumlah item di RecyclerView tetap kosong
        onView(withId(R.id.messageRecyclerView))
            .check(matches(not(hasDescendant(withText("")))))
    }
}
