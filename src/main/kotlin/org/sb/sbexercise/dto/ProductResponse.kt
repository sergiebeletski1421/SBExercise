package org.sb.sbexercise.dto

data class ProductResponse (
    val id: Long,
    val externalProductId: Long,
    val productName: String,
    val categoryName: String,
    val parts: List<PartResponse>
)