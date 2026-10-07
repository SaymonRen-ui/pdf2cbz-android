package com.saymon.pdf2cbz

import com.saymon.pdf2cbz.core.convert.etaText
import com.saymon.pdf2cbz.core.convert.pageName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterTest {
    @Test
    fun namesPadByTotalWidth() {
        assertEquals("001.jpg", pageName(0, 240))
        assertEquals("240.jpg", pageName(239, 240))
        assertEquals("01.jpg", pageName(0, 10))
        assertEquals("1.jpg", pageName(0, 9))
    }

    @Test
    fun etaOnlyAfterFirstFile() {
        assertEquals("", etaText(emptyList(), 0, 5))
        assertEquals("", etaText(listOf(1000L), 5, 5))
        val s = etaText(listOf(60_000L), 1, 3)
        assertTrue(s.startsWith("осталось ~"))
    }
}
