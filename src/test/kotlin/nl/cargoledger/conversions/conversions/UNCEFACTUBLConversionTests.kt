package nl.cargoledger.conversions.conversions

import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import kotlin.test.Test

class UNCEFACTUBLConversionTests {

    private val conversion = ConversionManager().get(ConversionType.UNCEFACT, ConversionType.UBL)!!

    @Test
    fun uncefactUBLConversionTest() {
        val uncefact = conversion.convert(javaClass.classLoader.getResource("UNCEFACT.xml")!!.readText())
    }
}