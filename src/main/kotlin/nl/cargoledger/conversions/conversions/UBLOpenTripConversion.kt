package nl.cargoledger.conversions.conversions

import Conversion
import InvalidDocumentException
import com.helger.ubl23.UBL23Reader
import com.helger.ubl23.UBL23Validator
import com.squareup.moshi.Moshi
import nl.cargoledger.conversions.ConversionType
import org.openapitools.client.models.Consignment

class UBLOpenTripConversion : Conversion(ConversionType.UBL, ConversionType.OPENTRIP) {
    override fun convert(a: String): String {
        val ubl = try {
            UBL23Reader.waybill().read(a)!!
        } catch (e: Exception) {
            throw InvalidDocumentException(e.message ?: "Unknown error")
        }
        val errors = UBL23Validator.waybill().validate(ubl)
        if (errors.isNotEmpty) {
            throw InvalidDocumentException(errors.joinToString())
        }

        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(Consignment::class.java)

        val consignment = Consignment(entityType = Consignment.EntityType.consignment)

        return jsonAdapter.toJson(consignment)
    }
}