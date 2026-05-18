package org.sb.sbexercise.repository

import org.sb.sbexercise.dto.PartResponse
import org.sb.sbexercise.dto.ProductResponse
import org.springframework.dao.TransientDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Repository

@Repository
class ProductsRepository (
    private val jdbcTemplate: JdbcTemplate
) {
    @Retryable(
        retryFor = [TransientDataAccessException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 500, multiplier = 2.0)
    )
    fun getProducts(afterId: Long, limit: Int) : List<ProductResponse> {
        val sql = """
            SELECT 
                id, 
                external_product_id, 
                product_name, 
                category_name
            FROM products 
            WHERE id > ? 
            ORDER BY id
            LIMIT ?
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            ProductResponse(
                id = rs.getLong("id"),
                externalProductId = rs.getLong("external_product_id"),
                productName = rs.getString("product_name"),
                categoryName = rs.getString("category_name"),
                parts = emptyList()
            )
        }, afterId, limit)
    }

    @Retryable(
        retryFor = [TransientDataAccessException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 500, multiplier = 2.0)
    )
    fun searchProducts(
        afterId: Long,
        limit: Int,
        search: String?,
        category: String?
    ): List<ProductResponse> {

        val sql = """
        SELECT
            p.id,
            p.external_product_id,
            p.product_name,
            p.category_name
        FROM products p
        WHERE p.id > ?
          AND (CAST(? AS TEXT) IS NULL OR LOWER(p.category_name) = LOWER(?))
          AND (CAST(? AS TEXT) IS NULL OR LOWER(p.product_name) LIKE LOWER(?)
          OR EXISTS (
            SELECT 1
            FROM parts pt
            WHERE pt.product_id = p.id
                AND (
                    LOWER(pt.external_part_number) LIKE LOWER(?)
                        OR LOWER(pt.part_description) LIKE LOWER(?)
                        OR LOWER(pt.brand_name) LIKE LOWER(?)
                    )
            )
          )

        ORDER BY p.id
        LIMIT ?
    """.trimIndent()


        val searchPattern = "%${search ?: ""}%"


        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                ProductResponse(
                    id = rs.getLong("id"),
                    externalProductId = rs.getLong("external_product_id"),
                    productName = rs.getString("product_name"),
                    categoryName = rs.getString("category_name"),
                    parts = emptyList()
                )
            },
            afterId,

            // category filter
            category,
            category,

            // search filter
            search,

            // product_name
            searchPattern,

            // external_part_number
            searchPattern,

            // part_description
            searchPattern,

            // brand_name
            searchPattern,
            limit
        )
    }

    @Retryable(
        retryFor = [TransientDataAccessException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 500, multiplier = 2.0)
    )
    fun getPartsByProductIds(productIds: List<Long>) : Map<Long, List<PartResponse>> {
        val placeholders = productIds.joinToString(",") { "?" }

        val sql = """
            SELECT 
                id,
                product_id, 
                external_part_number, 
                part_description, 
                original_retail_price, 
                brand_name, 
                image_url 
            FROM parts
            WHERE product_id IN ($placeholders)
            ORDER BY product_id, id
        """.trimIndent()

        val result = jdbcTemplate.query(sql, { rs, _ ->
            PartRow (
                productId = rs.getLong("product_id"),
                part = PartResponse (
                    id = rs.getLong("id"),
                    externalPartNumber = rs.getString("external_part_number"),
                    partDescription = rs.getString("part_description"),
                    originalRetailPrice = rs.getBigDecimal("original_retail_price"),
                    brandName = rs.getString("brand_name"),
                    imageUrl = rs.getString("image_url")
                )
            )
        },  *productIds.toTypedArray())

        return result.groupBy(
            keySelector =  {it.productId},
            valueTransform = { it.part }
        )
    }

    private data class PartRow(
        val productId: Long,
        val part: PartResponse
    )
}