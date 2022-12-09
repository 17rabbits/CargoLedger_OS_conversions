package nl.cargoledger.conversions.conversions

import Conversion
import InvalidDocumentException
import com.google.gson.Gson
import com.helger.ubl23.UBL23Reader
import com.helger.ubl23.UBL23Validator
import nl.cargoledger.conversions.ConversionType
import nl.cargoledger.conversions.models.opentrip.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.AddressType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.GoodsItemType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.PartyType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.TransportHandlingUnitType
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.MeasureType
import java.math.BigDecimal
import java.util.*

class UBLOpenTripConversion : Conversion(ConversionType.UBL, ConversionType.OPENTRIP) {
    override fun convert(a: String): String {
        val waybill = try {
            UBL23Reader.waybill().read(a)!!
        } catch (e: Exception) {
            throw InvalidDocumentException(e.message ?: "Unknown error")
        }
        val errors = UBL23Validator.waybill().validate(waybill)
        if (errors.isNotEmpty) {
            throw InvalidDocumentException(errors.joinToString())
        }

        val shipment = waybill.shipment!!
        val consignment = shipment.consignment.first()

        val opentrip = Consignment(
            id = consignment.idValue!!,
            externalAttributes = ExternalAttributes(waybill.idValue!!, shipment.idValue!!),
            goods = consignment.transportHandlingUnit.map { transportEquipment(it) },
            documents = waybill.documentReference.mapNotNull { ref ->
                ref.attachment?.embeddedDocumentBinaryObject?.let { doc ->
                    Document(
                        ref.idValue!!, ref.documentTypeCodeValue ?: "unknown", doc.filename ?: "unknown", doc.mimeCode ?: "unknown",
                        DocumentContent(Base64.getEncoder().encodeToString(doc.value))
                    )
                }
            },
            actors = listOf(
                Actors(partyToActor(consignment.consigneeParty!!), listOf(ActorRole.consignee)),
                Actors(partyToActor(consignment.consignorParty!!), listOf(ActorRole.consignor)),
                Actors(partyToActor(consignment.carrierParty!!), listOf(ActorRole.carrier)),
            ),
            actions = listOf(
                Actions(Action(ActionType.load, Locations(Location(postalAddressToAddress(consignment.requestedPickupTransportEvent!!.location!!.address!!))))),
                Actions(Action(ActionType.unload, Locations(Location(postalAddressToAddress(consignment.requestedDeliveryTransportEvent!!.location!!.address!!)))))
            )
        )

        return Gson().toJson(opentrip)
    }

    private fun partyToActor(party: PartyType) = Actor(
        party.partyName.first().nameValue!!,
        listOf(Locations(Location(postalAddressToAddress(party.postalAddress!!))))
    )

    private fun postalAddressToAddress(address: AddressType) = Address(
        houseNumber = address.buildingNumberValue,
        houseNumberAddition = null,
        street = address.streetNameValue,
        postalCode = address.postalZoneValue,
        city = address.cityNameValue,
        country = address.country?.identificationCodeValue
    )

    private fun transportEquipment(unit: TransportHandlingUnitType): Goods {
        val container = measureToDimension(unit.measurementDimension.find { it.measure?.unitCode == "ldm" }?.measure)
        val pallet = measureToDimension(unit.measurementDimension.find { it.measure?.unitCode == "pp" }?.measure)
        return Goods(TransportEquipment(
            name = null,
            loadMeters = container ?: pallet,
            equipmentType = if (container != null) EquipmentType.loadCarrier else if (pallet != null) EquipmentType.pallet else EquipmentType.box,
            containedGoods = unit.goodsItem.map { items(it) }
        ))
    }

    private fun items(item: GoodsItemType) = Goods(
        Items(
            name = item.item.firstOrNull()?.nameValue,
            description = item.item.first().description.first().value!!
        )
    )

    private fun measureToDimension(measure: MeasureType?) =
        measure?.let { Dimension((it.value ?: BigDecimal.ZERO).toDouble(), it.unitCode ?: "") }
}