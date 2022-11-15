package nl.cargoledger.conversions.conversions

import Conversion
import InvalidDocumentException
import com.helger.ubl23.UBL23Validator
import com.helger.ubl23.UBL23Writer
import jakarta.xml.bind.JAXBContext
import nl.cargoledger.conversions.ConversionType
import nl.cargoledger.conversions.models.opentrip.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.*
import oasis.names.specification.ubl.schema.xsd.waybill_23.WaybillType
import un.unece.uncefact.data.standard.ecmr._131.ECMRType
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._131.LogisticsPackageType
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._131.SupplyChainConsignmentItemType
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._131.TradeAddressType
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._131.TradePartyType
import java.util.*

class UNCEFACTUBLConversion : Conversion(ConversionType.UNCEFACT, ConversionType.UBL) {
    override fun convert(a: String): String {
        val jaxbContext = JAXBContext.newInstance(ECMRType::class.java)
        val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        val uncefact = jaxbUnmarshaller.unmarshal(a.reader()) as ECMRType

        val consignment = uncefact.specifiedSupplyChainConsignment

        val waybill = WaybillType().apply {
            id = IDType(uncefact.exchangedDocument.id.value)
            shipment = ShipmentType().apply {
//                id = IDType(uncefact)
                addConsignment(ConsignmentType().apply {
                    id = IDType(consignment.id.value)
                    consigneeParty = actorToParty(consignment.consigneeTradeParty)
                    consignorParty = actorToParty(consignment.consignorTradeParty)
                    carrierParty = actorToParty(consignment.carrierTradeParty)
                    requestedPickupTransportEvent = TransportEventType().apply {
                        location = LocationType().apply { address = addressToPostalAddress(consignment.pickUpTransportEvent.occurrenceLogisticsLocation.postalTradeAddress) }
                    }
                    requestedDeliveryTransportEvent = TransportEventType().apply {
                        location = LocationType().apply { address = addressToPostalAddress(consignment.deliveryTransportEvent.occurrenceLogisticsLocation.postalTradeAddress) }
                    }
                    setTransportHandlingUnit(consignment.includedSupplyChainConsignmentItem.map { transportEquipment(it) })
                })
            }
            setDocumentReference(consignment.associatedReferencedDocument.map {
                DocumentReferenceType().apply {
                    id = IDType(it.id.value)
                    documentTypeCode = DocumentTypeCodeType(it.typeCode.value)
                    val file = it.attachedSpecifiedBinaryFile.first()
                    attachment = AttachmentType().apply {
                        embeddedDocumentBinaryObject = EmbeddedDocumentBinaryObjectType().apply {
                            filename = file.id.value
                            mimeCode = file.mimeCode.value
                            value = Base64.getDecoder().decode(file.uriid.value)
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

    private fun actorToParty(actor: TradePartyType) = PartyType().apply {
        addPartyName(PartyNameType().apply { name = NameType(actor.name.value) })
        postalAddress = addressToPostalAddress(actor.postalTradeAddress)
    }

    private fun addressToPostalAddress(address: TradeAddressType) = AddressType().apply {
        buildingNumber = BuildingNumberType().apply { value = address.id.first().value }
        streetName = StreetNameType().apply { value = address.streetName.value }
        postalZone = PostalZoneType().apply { value = address.postcodeCode.value }
        cityName = CityNameType().apply { value = address.cityName.value }
        country = CountryType().apply { identificationCode = IdentificationCodeType(address.countryID.value.value()) }
    }

    private fun transportEquipment(equipment: SupplyChainConsignmentItemType): TransportHandlingUnitType = TransportHandlingUnitType().apply {
        addPackage(PackageType().apply {
            setGoodsItem(equipment.transportLogisticsPackage.map { goodsItem(it) })
//            dimensionToMeasure(equipment)?.let {
//                addMeasurementDimension(DimensionType().apply { measure = it })
//            }
        })
    }

    private fun goodsItem(item: LogisticsPackageType) = GoodsItemType().apply {
        addItem(ItemType().apply {
            name = NameType(item.id.value)
            setDescription(item.type.map { DescriptionType(it.value) })
        })
    }

    private fun dimensionToMeasure(dimension: Dimension?) = dimension?.let {
        MeasureType().apply {
            value = it.value.toBigDecimal()
            unitCode = it.unit
        }
    }
}