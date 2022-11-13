package nl.cargoledger.conversions

import Conversion
import DoNothingConversion
import nl.cargoledger.conversions.conversions.OpenTripUBLConversion
import nl.cargoledger.conversions.conversions.UBLOpenTripConversion

enum class ConversionType {
    UBL, UNCEFACT, OPENTRIP
}

class ConversionManager {
    private val conversions = mutableSetOf(
        UBLOpenTripConversion(),
        OpenTripUBLConversion()
    )

    fun get(a: ConversionType, b: ConversionType) = get(a.toString(), b.toString())

    fun get(a: String, b: String) = if (a == b) {
        DoNothingConversion(a)
    } else {
        conversions.find { it.a == a && it.b == b }
    }

    fun put(conversion: Conversion) {
        conversions.add(conversion)
    }

    fun remove(conversion: Conversion) {
        conversions.remove(conversion)
    }
}