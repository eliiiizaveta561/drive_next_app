package com.drivenext.app.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarSearchTest {

    @Test
    fun normalizeTrimsAndCollapsesSpaces() {
        assertEquals("Mercedes Benz", CarSearch.normalize("  Mercedes    Benz "))
    }

    @Test
    fun normalizeRemovesCharactersThatBreakFilter() {
        assertEquals("S 500", CarSearch.normalize("S,500"))
        assertEquals("BMW", CarSearch.normalize("%BMW_(*)"))
    }

    @Test
    fun emptyOrSymbolsOnlyQueryIsInvalid() {
        assertFalse(CarSearch.isValid(""))
        assertFalse(CarSearch.isValid("   "))
        assertFalse(CarSearch.isValid("%,()"))
        assertTrue(CarSearch.isValid("kia"))
    }
}
