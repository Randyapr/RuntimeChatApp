package com.example.runtimechatapp.ui.auth


import org.junit.Assert.assertEquals
import org.junit.Test

class OTPActivityTest {

    @Test
    fun testOtpValidationWithOtpKosong() {
        val otpInput = ""
        val isValid = isOtpValid(otpInput)
        assertEquals(false, isValid)
    }

    @Test
    fun testOtpValidationWithIncompleteOtp() {
        val otpInput = "1234" // Kurang dari 6 digit
        val isValid = isOtpValid(otpInput)
        assertEquals(false, isValid)
    }

    // Fungsi untuk validasi OTP benar
    @Test
    fun testOtpValidationWithValidOtp() {
        val otpInput = "123456" // 6 digit valid
        val isValid = isOtpValid(otpInput)
        assertEquals(true, isValid)
    }

    @Test
    fun testOtpValidationWithNonNumericOtp() {
        val otpInput = "12a45b" // Mengandung karakter sanes-angka
        val isValid = isOtpValid(otpInput)
        assertEquals(false, isValid)
    }

    // Fungsi untuk validasi OTP
    private fun isOtpValid(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }
}
