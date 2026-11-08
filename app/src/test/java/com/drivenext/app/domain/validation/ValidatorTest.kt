package com.drivenext.app.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {

    @Test
    fun emailWithComOrRuIsValid() {
        assertTrue(Validator.isEmailValid("name@domain.com"))
        assertTrue(Validator.isEmailValid("name@domain.ru"))
    }

    @Test
    fun emailWithOtherDomainOrWithoutAtIsInvalid() {
        assertFalse(Validator.isEmailValid("name@domain.org"))
        assertFalse(Validator.isEmailValid("namedomain.com"))
        assertFalse(Validator.isEmailValid(""))
    }

    @Test
    fun licenseNumberMustHaveTenCharacters() {
        assertTrue(Validator.isLicenseNumberValid("1234567890"))
        assertFalse(Validator.isLicenseNumberValid("123456789"))
        assertFalse(Validator.isLicenseNumberValid("12345678901"))
    }

    @Test
    fun birthDateMustBeRealAndInThePast() {
        assertTrue(Validator.isBirthDateValid("15/03/2003"))
        assertFalse(Validator.isBirthDateValid("31/02/2003"))
        assertFalse(Validator.isBirthDateValid("01/01/2999"))
        assertFalse(Validator.isBirthDateValid("2003-03-15"))
    }

    @Test
    fun issueDateCannotBeInTheFuture() {
        assertTrue(Validator.isIssueDateValid("01/06/2022"))
        assertFalse(Validator.isIssueDateValid("01/06/2999"))
    }
}
