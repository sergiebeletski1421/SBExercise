package org.sb.sbexercise.controller

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.sb.sbexercise.dto.ProductsPageResponse
import org.sb.sbexercise.service.ProductsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ProductsController::class)
@Import(ApiExceptionHandler::class)
class ProductsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var productsService: ProductsService

    @Test
    fun `GET products returns products page`() {
        whenever(productsService.getProductsAndParts(0, 10, null, null))
            .thenReturn(
                ProductsPageResponse(
                    nextAfterId = null,
                    limit = 10,
                    products = emptyList()
                )
            )

        mockMvc.get("/products") {
            param("afterId", "0")
            param("limit", "10")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.limit") { value(10) }
                jsonPath("$.products") { isArray() }
            }
    }

    @Test
    fun `GET products rejects negative afterId`() {
        mockMvc.get("/products") {
            param("afterId", "-5")
            param("limit", "10")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { exists() }
        }
    }

    @Test
    fun `GET products rejects limit above 500`() {
        mockMvc.get("/products") {
            param("afterId", "0")
            param("limit", "999")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { exists() }
        }
    }

    @Test
    fun `GET products rejects limit below 1`() {
        mockMvc.get("/products") {
            param("afterId", "0")
            param("limit", "0")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { exists() }
        }
    }

    @Test
    fun `GET products rejects search longer than 50 chars`() {
        mockMvc.get("/products") {
            param("afterId", "0")
            param("limit", "10")
            param("search", "x".repeat(256))
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { exists() }
        }
    }
}