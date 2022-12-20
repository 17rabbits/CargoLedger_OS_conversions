package nl.cargoledger.conversions.conversions

import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import kotlin.test.Test

class UBLUNCFACTConversionTests {

    private val conversion = ConversionManager().get(ConversionType.UBL, ConversionType.UNCEFACT)!!

    @Test
    fun ublUNCEFACTConversionTest() {
        val uncefact = conversion.convert(javaClass.classLoader.getResource("UBL.xml")!!.readText())
    }
}