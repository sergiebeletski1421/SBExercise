package org.sb.sbexercise.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.sb.sbexercise.dto.PartResponse
import org.sb.sbexercise.dto.ProductResponse
import org.sb.sbexercise.repository.ProductsRepository
import java.math.BigDecimal

class ProductsServiceTest {

    private val productsRepository: ProductsRepository = mock()
    private val productsService = ProductsService(productsRepository)

    @Test
    fun `returns products with mapped parts`() {
        val product = ProductResponse(
            id = 1L,
            externalProductId = 1001L,
            productName = "FX-24 Solid Helmets Test",
            categoryName = "Adult Street Helmets Test",
            parts = emptyList()
        )

        val part = PartResponse(
            id = 10L,
            externalPartNumber = "P-100 Test",
            partDescription = "Description Test",
            originalRetailPrice = BigDecimal("99.99"),
            brandName = "BrandA Test",
            imageUrl = null
        )

        whenever(productsRepository.getProducts(0, 10))
            .thenReturn(listOf(product))

        whenever(productsRepository.getPartsByProductIds(listOf(1L)))
            .thenReturn(mapOf(1L to listOf(part)))

        val response = productsService.getProductsAndParts(0, 10,null, null)

        assertEquals(10, response.limit)
        assertNull(response.nextAfterId)
        assertEquals(1, response.products.size)
        assertEquals(1, response.products[0].parts.size)
        assertEquals("Description Test", response.products[0].parts[0].partDescription)
    }

    @Test
    fun `returns products for search query`() {

        val product = ProductResponse(
            id = 1L,
            externalProductId = 1001L,
            productName = "TestP Helmet",
            categoryName = "Helmets",
            parts = emptyList()
        )

        val part = PartResponse(
            id = 10L,
            externalPartNumber = "TSTP-123",
            partDescription = "TestP Replacement Shield",
            originalRetailPrice = BigDecimal("49.99"),
            brandName = "TSTP",
            imageUrl = null
        )

        whenever(
            productsRepository.searchProducts(
                afterId = 0,
                limit = 10,
                search = "testp",
                category = null
            )
        ).thenReturn(listOf(product))

        whenever(
            productsRepository.getPartsByProductIds(listOf(1L))
        ).thenReturn(
            mapOf(1L to listOf(part))
        )

        val response = productsService.getProductsAndParts(
            afterId = 0,
            limit = 10,
            search = "testp",
            category = null
        )

        assertEquals(10, response.limit)
        assertEquals(1, response.products.size)

        assertEquals(
            "TestP Helmet",
            response.products[0].productName
        )

        assertEquals(
            1,
            response.products[0].parts.size
        )

        assertEquals(
            "TSTP",
            response.products[0].parts[0].brandName
        )
    }

    @Test
    fun `returns empty response when no products found`() {
        whenever(productsRepository.getProducts(0, 10))
            .thenReturn(emptyList())

        val response = productsService.getProductsAndParts(0, 10, null, null)

        assertNull(response.nextAfterId)
        assertEquals(10, response.limit)
        assertEquals(emptyList<ProductResponse>(), response.products)
    }

    @Test
    fun `normalizes whitespace-only search to null`() {
        whenever(productsRepository.getProducts(0, 10))
            .thenReturn(emptyList())
        val response = productsService.getProductsAndParts(0, 10, "   ", "  ")
        assertEquals(emptyList<ProductResponse>(), response.products)
    }
}