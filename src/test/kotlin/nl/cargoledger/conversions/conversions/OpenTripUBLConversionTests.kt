package nl.cargoledger.conversions.conversions

import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import kotlin.test.Test

class OpenTripUBLConversionTests {

    private val conversion = ConversionManager().get(ConversionType.OPENTRIP, ConversionType.UBL)!!

    @Test
    fun ublOpenTripConversionTest() {
        val opentrip = conversion.convert(javaClass.classLoader.getResource("Opentrip.json")!!.readText())
    }
}