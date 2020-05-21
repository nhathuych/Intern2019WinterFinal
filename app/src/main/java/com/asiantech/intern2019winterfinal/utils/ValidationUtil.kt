package com.asiantech.intern2019winterfinal.utils

import java.util.regex.Pattern

object ValidationUtil {
    private const val PATTERN_EMAIL = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$"
    private const val PATTERN_PASSWORD = "^[a-zA-Z0-9\\+]*$"

    fun validateEmail(email: String): Boolean {
        return Pattern.compile(PATTERN_EMAIL, Pattern.CASE_INSENSITIVE).matcher(email).find()
    }

    fun validatePassword(password: String): Boolean {
        return Pattern.compile(PATTERN_PASSWORD, Pattern.CASE_INSENSITIVE).matcher(password).find()
    }
}
