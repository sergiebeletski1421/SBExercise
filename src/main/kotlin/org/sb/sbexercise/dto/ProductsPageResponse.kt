package org.sb.sbexercise.dto

data class ProductsPageResponse (
    val nextAfterId: Long?,
    val limit: Int,
    val products: List<ProductResponse>
)