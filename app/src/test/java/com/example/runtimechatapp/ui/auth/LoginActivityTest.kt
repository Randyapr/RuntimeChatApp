package com.example.runtimechatapp.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginActivityTest {

    // jika nomor phone valid
    @Test
    fun testValidPhoneNumber() {
        val validPhoneNumber = "+62864169419"
        val isValid = isValidPhoneNumber(validPhoneNumber)
        assertEquals("Nomor telepon harus valid: $validPhoneNumber", true, isValid)
        System.out.println("Test testValidPhoneNumber berhasil: Nomor $validPhoneNumber valid.")
    }

    // jika nomor phone tidak valid
    @Test
    fun testInvalidPhoneNumberShort() {
        val invalidPhoneNumber = "+62812"
        val isValid = isValidPhoneNumber(invalidPhoneNumber)
        assertEquals("Nomor telepon terlalu pendek: $invalidPhoneNumber", false, isValid)
        System.out.println("Test testInvalidPhoneNumberShort berhasil: Nomor $invalidPhoneNumber tidak valid.")
    }

    // jika nomor phone kosong
    @Test
    fun testEmptyPhoneNumber() {
        val emptyPhoneNumber = ""
        val isValid = isValidPhoneNumber(emptyPhoneNumber)
        assertEquals("Nomor telepon tidak boleh kosong", false, isValid)
        System.out.println("Test testEmptyPhoneNumber berhasil: Nomor telepon kosong tidak valid.")
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotEmpty() && phoneNumber.length >= 10
    }
}