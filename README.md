# Conversions
This repository enables you to convert between the following formats: UBL, UNCEFACT and Opentrip.

# Usage
Use the ConversionManager to get a specific Conversion

`val conversion = ConversionManager().get(ConversionType.UNCEFACT, ConversionType.UBL)`

Use the conversion to convert between a format

``val result = conversion.convert(<input source as string>)``