package nl.cargoledger.conversions

import DoNothingConversion
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversionManagerTests {

    private val manager = ConversionManager()

    @Test
    fun getDoNothingConversion() {
        assertEquals(DoNothingConversion(ConversionType.UBL), manager.get(ConversionType.UBL, ConversionType.UBL))
        assertEquals(DoNothingConversion(ConversionType.OPENTRIP), manager.get(ConversionType.OPENTRIP, ConversionType.OPENTRIP))
        assertEquals(DoNothingConversion(ConversionType.UNCEFACT), manager.get(ConversionType.UNCEFACT, ConversionType.UNCEFACT))
    }
}