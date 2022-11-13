package nl.cargoledger.conversions.models.opentrip

data class Documents(override val entity: Document) : Entity<Document>

data class Document(
    val id: String,
    val documentType: String,
    val filename: String,
    val mimeType: String,
    val content: DocumentContent
)

data class DocumentContent(
    val raw: String
) {
    val contentType = "data"
}