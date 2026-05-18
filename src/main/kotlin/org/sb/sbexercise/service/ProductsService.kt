package org.sb.sbexercise.service

import org.sb.sbexercise.dto.ProductsPageResponse
import org.sb.sbexercise.repository.ProductsRepository
import org.springframework.dao.TransientDataAccessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductsService (
    private val productsRepository: ProductsRepository
) {

    @Transactional(readOnly = true)
    fun getProductsAndParts(afterId: Long,
                            limit: Int,
                            search: String?,
                            category: String?) : ProductsPageResponse {

        val normalizedSearch = search?.trim()?.takeUnless { it.isBlank() }
        val normalizedCategory = category?.trim()?.takeUnless { it.isBlank() }

        val products = if (normalizedSearch == null && normalizedCategory == null) {
            productsRepository.getProducts(afterId, limit)
        } else {
            productsRepository.searchProducts(
                afterId = afterId,
                limit = limit,
                search = normalizedSearch,
                category = normalizedCategory
            )
        }

        if (products.isEmpty()) {
            return ProductsPageResponse(null, limit, emptyList())
        }

        val productIds = products.map { it.id }

        val parts = productsRepository.getPartsByProductIds(productIds)

        val productsWithParts = products.map { product ->
            product.copy(parts = parts[product.id] ?: emptyList()) }

        val nextAfterId = if (products.size == limit) {
            products.last().id
        } else {
            null
        }

        return ProductsPageResponse(nextAfterId, limit, productsWithParts)
    }
}