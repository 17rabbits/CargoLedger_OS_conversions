package nl.cargoledger.conversions.models.opentrip

data class Goods(override val entity: TransportEntity) : Entity<TransportEntity>

data class TransportEntity(
    val name: String?,
    val loadMeters: Dimension?,
    val equipmentType: EquipmentType,
    val description: String?,
    val type: EntityType,
    val productType: String?
)

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