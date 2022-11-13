package nl.cargoledger.conversions.conversions

import Conversion
import InvalidDocumentException
import com.google.gson.Gson
import com.helger.ubl23.UBL23Validator
import com.helger.ubl23.UBL23Writer
import nl.cargoledger.conversions.ConversionType
import nl.cargoledger.conversions.models.opentrip.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.*
import oasis.names.specification.ubl.schema.xsd.waybill_23.WaybillType
import java.util.*

class OpenTripUBLConversion : Conversion(ConversionType.OPENTRIP, ConversionType.UBL) {
    override fun convert(a: String): String {
        val consignment = try {
            Gson().fromJson(a, Consignment::class.java)
        } catch (e: Exception) {
            throw InvalidDocumentException(e.message ?: "Unknown error")
        }

        val waybill = WaybillType().apply {
            id = IDType(consignment.externalAttributes.ecmrId)
            shipment = ShipmentType().apply {
                id = IDType(consignment.externalAttributes.shipmentId)
                addConsignment(ConsignmentType().apply {
                    id = IDType(consignment.id)
                    consigneeParty = actorToParty(consignment.actors.find { it.roles.contains(ActorRole.consignee) }!!.entity)
                    consignorParty = actorToParty(consignment.actors.find { it.roles.contains(ActorRole.consignor) }!!.entity)
                    carrierParty = actorToParty(consignment.actors.find { it.roles.contains(ActorRole.carrier) }!!.entity)
                    requestedPickupTransportEvent = TransportEventType().apply {
                        location = LocationType().apply { address = addressToPostalAddress(consignment.actions.find { it.entity.actionType == ActionType.load }!!.entity.location.entity.administrativeReference) }
                    }
                    requestedDeliveryTransportEvent = TransportEventType().apply {
                        location = LocationType().apply { address = addressToPostalAddress(consignment.actions.find { it.entity.actionType == ActionType.unload }!!.entity.location.entity.administrativeReference) }
                    }
                    setTransportHandlingUnit(consignment.goods.map { transportEquipment(it.entity as TransportEquipment) })
                })
            }
            setDocumentReference(consignment.documents.map {
                DocumentReferenceType().apply {
                    id = IDType(it.id)
                    documentTypeCode = DocumentTypeCodeType(it.documentType)
                    attachment = AttachmentType().apply {
                        embeddedDocumentBinaryObject = EmbeddedDocumentBinaryObjectType().apply {
                            filename = it.filename
                            mimeCode = it.mimeType
                            value = Base64.getDecoder().decode(it.content.raw)
                        }
                    }
                }
            })
        }

        val errors = UBL23Validator.waybill().validate(waybill)
        if (errors.isNotEmpty) {
            throw InvalidDocumentException(errors.joinToString())
        }

        return UBL23Writer.waybill().getAsString(waybill)!!
    }

    private fun actorToParty(actor: Actor) = PartyType().apply {
        addPartyName(PartyNameType().apply { name = NameType(actor.name) })
        postalAddress = addressToPostalAddress(actor.locations.first().entity.administrativeReference)
    }

    private fun addressToPostalAddress(address: Address) = AddressType().apply {
        buildingNumber = BuildingNumberType().apply { value = address.houseNumber }
        streetName = StreetNameType().apply { value = address.street }
        postalZone = PostalZoneType().apply { value = address.postalCode }
        cityName = CityNameType().apply { value = address.city }
        country = CountryType().apply { identificationCode = IdentificationCodeType(address.country) }
    }

    private fun transportEquipment(equipment: TransportEquipment): TransportHandlingUnitType = TransportHandlingUnitType().apply {
        addPackage(PackageType().apply {
            setGoodsItem(equipment.containedGoods.map { goodsItem(it.entity as Items) })
            dimensionToMeasure(equipment.loadMeters)?.let {
                addMeasurementDimension(DimensionType().apply { measure = it })
            }
        })
    }

    private fun goodsItem(item: Items) = GoodsItemType().apply {
        addItem(ItemType().apply {
            name = NameType(item.name)
            addDescription(DescriptionType(item.description))
        })
    }

    private fun dimensionToMeasure(dimension: Dimension?) = dimension?.let {
        MeasureType().apply {
            value = it.value.toBigDecimal()
            unitCode = it.unit
        }
    }
}