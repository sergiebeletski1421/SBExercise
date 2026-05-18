package org.sb.sbexercise.controller

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.sb.sbexercise.dto.ProductsPageResponse
import org.sb.sbexercise.service.ProductsService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/products")
class ProductsController(
    private val productsService: ProductsService
) {

    @GetMapping
    fun getProductsAndParts(
        @RequestParam(defaultValue = "0") @Min(0) afterId: Long,
        @RequestParam(defaultValue = "100") @Min(1) @Max(500) limit: Int,
        @RequestParam(required = false) @Size(max = 200) search: String?,
        @RequestParam(required = false) @Size(max = 255) category: String?
    ) : ProductsPageResponse {
        return productsService.getProductsAndParts(afterId, limit, search, category)
    }
}