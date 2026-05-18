package org.sb.sbexercise.dto

import java.math.BigDecimal

data class PartResponse(
    val id: Long,
    val externalPartNumber: String,
    val partDescription: String,
    val originalRetailPrice: BigDecimal,
    val brandName: String,
    val imageUrl: String?
)