package nl.cargoledger.conversions.models.opentrip

data class Actors(
    override val entity: Actor,
    val roles: List<ActorRole>
) : Entity<Actor>

data class Actor(
    val name: String,
    val locations: List<Locations>
)

enum class ActorRole {
    shipper, carrier, consignee, consignor, receiver, driver, subcontractor
}