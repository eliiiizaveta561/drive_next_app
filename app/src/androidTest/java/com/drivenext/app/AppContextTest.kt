package com.drivenext.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContextTest {

    @Test
    fun packageNameIsDriveNext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.drivenext.app", context.packageName)
    }
}
