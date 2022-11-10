import nl.cargoledger.conversions.ConversionType

abstract class Conversion(val a: String, val b: String) {
    constructor(a: ConversionType, b: ConversionType) : this(a.toString(), b.toString())
    abstract fun convert(a: String): String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Conversion) return false

        if (a != other.a) return false
        if (b != other.b) return false

        return true
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }
}

class InvalidDocumentException(message: String) : Exception(message)

class DoNothingConversion(type: String) : Conversion(type, type) {
    constructor(type: ConversionType) : this(type.toString())
    override fun convert(a: String) = a
}