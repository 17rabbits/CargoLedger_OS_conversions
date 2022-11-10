package nl.cargoledger.conversions.conversions

import com.helger.ubl23.UBL23Writer
import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import oasis.names.specification.ubl.schema.xsd.waybill_23.WaybillType
import kotlin.test.Test

class UBLOpenTripConversionTests {

    private val conversion = ConversionManager().get(ConversionType.UBL, ConversionType.OPENTRIP)!!

    @Test
    fun ublOpenTripConversionTest() {
        val waybill = WaybillType()
        conversion.convert(UBL23Writer.waybill().getAsString(waybill)!!)
    }
}