package nl.cargoledger.conversions.models.opentrip

data class Locations(override val entity: Location) : Entity<Location>

data class Location(
    val name: String?,
    val administrativeReference: Address
)

data class Address(
    val houseNumber: String?,
    val houseNumberAddition: String?,
    val street: String?,
    val postalCode: String?,
    val city: String?,
    val country: String?
)