# SBExercise — Products API

A small Spring Boot + Kotlin service that exposes a paginated, searchable
catalog of products and their parts.

## Stack

- Kotlin 2.2 on JDK 17
- Spring Boot 4.0 (Web MVC, JDBC, Validation, Retry)
- PostgreSQL 16
- Maven (wrapper included), JUnit 5, Mockito-Kotlin

## Quick start

Prerequisites: Docker, JDK 17.

```
# 1. Start Postgres + load the schema and sample data
docker compose up -d

# 2. Run the app
./mvnw spring-boot:run

# 3. Hit the endpoint
curl -s "http://localhost:8080/products" | jq .
```

The app listens on `:8080`. Postgres is on `:5432` (db `products`, user/pass `postgres`/`postgres`).

To stop and reset:

```
docker compose down -v   # -v wipes the volume so schema.sql re-runs next start
```

## API

One endpoint: `GET /products`.

Response shape:

```json
{
  "nextAfterId": 10,
  "limit": 10,
  "products": [
    {
      "id": 1,
      "externalProductId": 1287,
      "productName": "AX-8 Carbon Helmets",
      "categoryName": "Adult MX Helmets",
      "parts": [
        {
          "id": 42,
          "externalPartNumber": "0101-3337",
          "partDescription": "HELMET FX90 BLACK XS",
          "originalRetailPrice": 89.95,
          "brandName": "AFX",
          "imageUrl": "https://..."
        }
      ]
    }
  ]
}
```

### Example requests

```bash
# First page
curl -s "localhost:8080/products?limit=5" | jq .

# Next page
curl -s "localhost:8080/products?afterId=5&limit=5" | jq .

# Search by part number
curl -s "localhost:8080/products?search=0101-3337" | jq .

# Filter by category (case-insensitive)
curl -s --get "localhost:8080/products" \
  --data-urlencode "category=Adult MX Helmets" \
  --data-urlencode "limit=5" | jq .

# Combined
curl -s --get "localhost:8080/products" \
  --data-urlencode "category=Adult Street Helmets" \
  --data-urlencode "search=FX" | jq .

# Invalid input → 400
curl -i -s "localhost:8080/products?limit=999"
```
