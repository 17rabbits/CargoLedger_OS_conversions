package nl.cargoledger.conversions.models.opentrip

data class Consignment(
    val id: String,
    val externalAttributes: ExternalAttributes,
    val goods: List<Goods>,
    val documents: List<Document>,
    val actors: List<Actors>,
    val actions: List<Actions>
)

data class Actions(override val entity: Action) : Entity<Action>

data class Action(
    val actionType: ActionType,
    val location: Locations
)

enum class ActionType {
    load, unload
}

data class ExternalAttributes(
    val ecmrId: String,
    val shipmentId: String
)

interface Entity<T : Any>{
    val entity: T
    val associationType get() = "inline"
}
