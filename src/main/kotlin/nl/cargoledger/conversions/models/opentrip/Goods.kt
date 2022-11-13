package nl.cargoledger.conversions.models.opentrip

data class Goods(override val entity: TransportEntity) : Entity<TransportEntity>

sealed interface TransportEntity {
    val name: String?
    val type: EntityType
}

data class TransportEquipment(
    override val name: String?,
    val loadMeters: Dimension?,
    val containedGoods: List<Goods>,
    val equipmentType: EquipmentType
) : TransportEntity {
    override val type = EntityType.transportEquipment
}

data class Items(
    override val name: String?,
    val description: String
) : TransportEntity {
    override val type = EntityType.items
}

data class Dimension(
    val value: Double,
    val unit: String
)

enum class EntityType {
    transportEquipment, items
}

enum class EquipmentType {
    trailer, box, loadCarrier, pallet
}