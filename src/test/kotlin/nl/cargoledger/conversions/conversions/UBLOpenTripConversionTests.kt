package nl.cargoledger.conversions.conversions

import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import kotlin.test.Test

class UBLOpenTripConversionTests {

    private val conversion = ConversionManager().get(ConversionType.UBL, ConversionType.OPENTRIP)!!

    @Test
    fun ublOpenTripConversionTest() {
        val opentrip = conversion.convert(javaClass.classLoader.getResource("UBL.xml")!!.readText())
    }
}