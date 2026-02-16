
# Marketplace Application

A Spring Boot project for the GlobalDatabase internship program. This application demonstrates backend development skills, including REST API design, security, database migrations, and testing.

## Features

- Product management (CRUD, search, like/dislike)
- User authentication and registration (Spring Security)
- Database migrations (Liquibase)
- MyBatis integration
- Swagger API documentation
- Bulk product loading via CSV (PostgreSQL COPY)
- Integration tests with TestContainers

## Requirements

- Java 11
- Spring Boot 3.0.0
- PostgreSQL 14

## Getting Started

1. Install Java 11 and PostgreSQL 14.
2. Create a PostgreSQL database named `marketplace`.
3. Clone this repository.
4. Build the project:
   ```
   mvn clean install -DskipTests
   ```
5. Run the application:
   ```
   mvn spring-boot:run
   ```

## API Endpoints

- `POST /products` — Add a product
- `GET /products/{id}` — Get product by ID
- `PUT /products/{id}` — Edit product
- `DELETE /products/{id}` — Delete product
- `GET /products` — List products (pagination, search)
- `POST /auth/register` — Register user
- `POST /auth/login` — User login
- `POST /products/{id}/like` — Like a product
- `POST /products/{id}/dislike` — Dislike a product
- `POST /loading/products` — Bulk load products from CSV

## Documentation

- Swagger UI available at `/swagger-ui.html` (after app start)
- See [Spring Boot](https://spring.io/) and [MyBatis](https://mybatis.org/spring/boot.html) docs for framework details

## Testing

- Integration tests use TestContainers for isolated database testing.
- Run tests:
  ```
  mvn test
  ```

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Open a pull request for review.

## License

This project is licensed under the MIT License.
