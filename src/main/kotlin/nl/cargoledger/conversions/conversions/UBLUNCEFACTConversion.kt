package nl.cargoledger.conversions.conversions

import Conversion
import InvalidDocumentException
import com.google.gson.Gson
import com.helger.ubl23.UBL23Reader
import com.helger.ubl23.UBL23Validator
import jakarta.xml.bind.JAXBContext
import nl.cargoledger.conversions.ConversionType
import nl.cargoledger.conversions.models.opentrip.*
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.AddressType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.GoodsItemType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.PartyType
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.TransportHandlingUnitType
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.MeasureType
import un.unece.uncefact.data.standard.ecmr._131.ECMRType
import un.unece.uncefact.data.standard.qualifieddatatype._131.CountryIDType
import un.unece.uncefact.data.standard.qualifieddatatype._131.DocumentCodeType
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._131.*
import un.unece.uncefact.data.standard.unqualifieddatatype._131.CodeType
import un.unece.uncefact.data.standard.unqualifieddatatype._131.IDType
import un.unece.uncefact.data.standard.unqualifieddatatype._131.NumericType
import un.unece.uncefact.data.standard.unqualifieddatatype._131.TextType
import un.unece.uncefact.identifierlist.standard.iso.isotwo_lettercountrycode.secondedition2006.ISOTwoletterCountryCodeContentType
import java.io.StringWriter
import java.math.BigDecimal
import java.util.*
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.apply
import kotlin.let


class UBLUNCEFACTConversion : Conversion(ConversionType.UBL, ConversionType.UNCEFACT) {
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

        val uncefact = ECMRType().apply {
            exchangedDocument = ExchangedDocumentType().apply {
                id = IDType().apply { waybill.idValue!! }
            }
            specifiedSupplyChainConsignment = SupplyChainConsignmentType().apply {
                id = IDType().apply { consignment.idValue!! }
                consignment.transportHandlingUnit.forEachIndexed { index, unit -> includedSupplyChainConsignmentItem.add(transportEquipment(unit, index)) }
                waybill.documentReference.forEach { ref ->
                    ref.attachment?.embeddedDocumentBinaryObject?.let { doc ->
                        associatedReferencedDocument.add(ReferencedDocumentType().apply {
                            id = IDType().apply { value = ref.idValue!! }
                            typeCode = DocumentCodeType().apply { value = ref.documentTypeCodeValue!! }
                            attachedSpecifiedBinaryFile.add(SpecifiedBinaryFileType().apply {
                                id = IDType().apply { value = doc.filename!! }
                                uriid = IDType().apply { value = Base64.getEncoder().encodeToString(doc.value) }
                                mimeCode = CodeType().apply { value = doc.mimeCode!! }

                            })
                        })
                        Document(
                            ref.idValue!!, ref.documentTypeCodeValue!!, doc.filename!!, doc.mimeCode!!,
                            DocumentContent(Base64.getEncoder().encodeToString(doc.value))
                        )
                    }
                }
                consigneeTradeParty = partyToActor(consignment.consigneeParty!!)
                consignorTradeParty = partyToActor(consignment.consignorParty!!)
                carrierTradeParty = partyToActor(consignment.carrierParty!!)
                pickUpTransportEvent = TransportEventType().apply { occurrenceLogisticsLocation = LogisticsLocationType().apply { postalTradeAddress = postalAddressToAddress(consignment.requestedPickupTransportEvent!!.location!!.address!!) } }
                deliveryTransportEvent = TransportEventType().apply { occurrenceLogisticsLocation = LogisticsLocationType().apply { postalTradeAddress = postalAddressToAddress(consignment.requestedDeliveryTransportEvent!!.location!!.address!!) } }
            }
        }

        val jaxbContext: JAXBContext = JAXBContext.newInstance(ECMRType::class.java)
        val jaxbMarshaller = jaxbContext.createMarshaller()
        val sw = StringWriter()
        jaxbMarshaller.marshal(uncefact, sw)
        return sw.toString()
    }

    private fun partyToActor(party: PartyType) = TradePartyType().apply {
        name = TextType().apply { value = party.partyName.first().nameValue!! }
        postalTradeAddress = postalAddressToAddress(party.postalAddress!!)
    }

    private fun postalAddressToAddress(address: AddressType) = TradeAddressType().apply {
        id.add(IDType().apply { value = address.buildingNumberValue })
        streetName = TextType().apply { value = address.streetNameValue }
        postcodeCode = CodeType().apply { value = address.postalZoneValue }
        cityName = TextType().apply { value = address.cityNameValue }
        countryID = address.country?.identificationCodeValue?.let { CountryIDType().apply { value = ISOTwoletterCountryCodeContentType.valueOf(it) } }
    }

    private fun transportEquipment(unit: TransportHandlingUnitType, sequence: Int): SupplyChainConsignmentItemType {
        val container = measureToDimension(unit.measurementDimension.find { it.measure?.unitCode == "ldm" }?.measure)
        val pallet = measureToDimension(unit.measurementDimension.find { it.measure?.unitCode == "pp" }?.measure)

        return SupplyChainConsignmentItemType().apply {
            sequenceNumeric = NumericType().apply { value = BigDecimal(sequence + 1) }
            unit.goodsItem.forEach { transportLogisticsPackage.add(items(it)) }
        }
    }

    private fun items(item: GoodsItemType) = LogisticsPackageType().apply {
        id = IDType().apply { value = item.item.firstOrNull()?.nameValue }
        type.add(TextType().apply { value = item.item.first().description.first().value!! })
    }

    private fun measureToDimension(measure: MeasureType?) =
        measure?.let { Dimension((it.value ?: BigDecimal.ZERO).toDouble(), it.unitCode ?: "") }
}