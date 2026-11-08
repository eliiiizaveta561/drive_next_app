package com.drivenext.app.domain.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object Validator {

    const val LICENSE_NUMBER_LENGTH = 10
    private const val MAX_AGE_YEARS = 120L

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|ru)$")

    val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun isEmailValid(email: String): Boolean = emailRegex.matches(email.trim())

    fun isBirthDateValid(text: String): Boolean {
        val date = parseDate(text) ?: return false
        val today = LocalDate.now()
        return date.isBefore(today) && date.isAfter(today.minusYears(MAX_AGE_YEARS))
    }

    fun isIssueDateValid(text: String): Boolean {
        val date = parseDate(text) ?: return false
        return !date.isAfter(LocalDate.now())
    }

    fun isLicenseNumberValid(text: String): Boolean = text.trim().length == LICENSE_NUMBER_LENGTH

    private fun parseDate(text: String): LocalDate? =
        try {
            LocalDate.parse(text, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
}
