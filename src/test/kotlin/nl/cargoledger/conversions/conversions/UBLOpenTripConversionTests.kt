package nl.cargoledger.conversions.conversions

import com.helger.ubl23.UBL23Validator
import com.helger.ubl23.UBL23Writer
import nl.cargoledger.conversions.ConversionManager
import nl.cargoledger.conversions.ConversionType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.ShipmentType
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType
import oasis.names.specification.ubl.schema.xsd.waybill_23.WaybillType
import kotlin.test.Test

class UBLOpenTripConversionTests {

    private val conversion = ConversionManager().get(ConversionType.UBL, ConversionType.OPENTRIP)!!

    @Test
    fun ublOpenTripConversionTest() {
        val waybill = WaybillType().apply {
            id = IDType("Demo Waybill")
            shipment = ShipmentType().apply {
                id = IDType("Demo Shipment")
            }
        }
        val errors = UBL23Validator.waybill().validate(waybill)
        assert(errors.isEmpty) {
            errors
        }
        UBL23Writer.waybill().getAsString(waybill)!!
        conversion.convert(UBL23Writer.waybill().getAsString(waybill)!!)
    }
}