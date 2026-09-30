package com.microservice.justanotherapp.config;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.job.api.dto.UserDto;
import com.microservice.justanotherapp.dto.ExpenseRequestDto;
import com.microservice.justanotherapp.dto.ExpenseResponseDto;
import com.microservice.justanotherapp.dto.ExpenseTotalDto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Centralised OpenAPI documentation config.
 *
 * All @Operation, @ApiResponses, @Tag annotations have been removed
 * from controllers — this class is the single source of truth for
 * API documentation. Controllers stay clean.
 *
 * DTOs retain @Schema annotations for field-level documentation only
 * (descriptions, examples, constraints) — that's the right boundary.
 */
@Configuration
public class OpenApiConfig {

    // ── API Info ──────────────────────────────────────────────────────────

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("another-service API")
                        .description("User, Category and Expense management REST API")
                        .version("v1")
                        .contact(new Contact()
                                .name("Tejas Nambiar")
                                .email("tejas@email.com")))
                .tags(List.of(
                        new Tag().name("Users").description("User management endpoints"),
                        new Tag().name("Categories").description("Category lookup endpoints"),
                        new Tag().name("Expenses").description("Expense tracking endpoints")
                ));
    }

    // ── Operation documentation ───────────────────────────────────────────
    // Uses OpenApiCustomizer to attach operation metadata to paths that
    // springdoc auto-discovers from @RestController classes.
    // This keeps controllers completely annotation-free while still
    // generating a full spec at /v3/api-docs and /swagger-ui.html

    @Bean
    public OpenApiCustomizer expenseApiDocs() {
        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths == null) return;

            // ── POST /api/expenses ────────────────────────────────────────
            PathItem expensesPath = paths.getOrDefault("/api/expenses", new PathItem());
            expensesPath.setPost(new Operation()
                    .addTagsItem("Expenses")
                    .summary("Create a new expense")
                    .description("Creates an expense. Validates amount > 0, userId and categoryId must exist.")
                    .requestBody(jsonRequestBody(ExpenseRequestDto.class))
                    .responses(new ApiResponses()
                            .addApiResponse("201", jsonResponse("Expense created", ExpenseResponseDto.class))
                            .addApiResponse("400", messageResponse("Validation failed — amount ≤ 0, missing fields, etc"))
                            .addApiResponse("404", messageResponse("userId or categoryId not found"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));
            paths.addPathItem("/api/expenses", expensesPath);

            // ── GET /api/expenses ─────────────────────────────────────────
            expensesPath.setGet(new Operation()
                    .addTagsItem("Expenses")
                    .summary("List expenses with optional filters")
                    .description("""
                            Returns a paginated list of expenses.
                            Filterable by categoryId and a date range on expenseDate.
                            Default sort: expenseDate DESC. Pagination: page (0-based), size, sort.
                            """)
                    .addParametersItem(queryParam("categoryId", "integer", "Filter by category ID", false))
                    .addParametersItem(queryParam("startDate", "string", "Start date (YYYY-MM-DD), inclusive", false))
                    .addParametersItem(queryParam("endDate", "string", "End date (YYYY-MM-DD), inclusive", false))
                    .addParametersItem(queryParam("page", "integer", "Page number (0-based, default 0)", false))
                    .addParametersItem(queryParam("size", "integer", "Page size (default 20)", false))
                    .responses(new ApiResponses()
                            .addApiResponse("200", messageResponse("Paginated expense list"))
                            .addApiResponse("400", messageResponse("Invalid filter params"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));

            // ── GET /api/expenses/total ───────────────────────────────────
            PathItem totalPath = paths.getOrDefault("/api/expenses/total", new PathItem());
            totalPath.setGet(new Operation()
                    .addTagsItem("Expenses")
                    .summary("Get aggregate total for filtered expenses")
                    .description("""
                            Returns { total: BigDecimal, count: long } via a single SUM/COUNT query.
                            Same filters as the list endpoint. Never loads rows into memory.
                            """)
                    .addParametersItem(queryParam("categoryId", "integer", "Filter by category ID", false))
                    .addParametersItem(queryParam("startDate", "string", "Start date (YYYY-MM-DD), inclusive", false))
                    .addParametersItem(queryParam("endDate", "string", "End date (YYYY-MM-DD), inclusive", false))
                    .responses(new ApiResponses()
                            .addApiResponse("200", jsonResponse("Aggregate result", ExpenseTotalDto.class))
                            .addApiResponse("400", messageResponse("Invalid filter params"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));
            paths.addPathItem("/api/expenses/total", totalPath);

            // ── GET /api/categories ───────────────────────────────────────
            PathItem categoriesPath = paths.getOrDefault("/api/categories", new PathItem());
            categoriesPath.setGet(new Operation()
                    .addTagsItem("Categories")
                    .summary("List all categories")
                    .description("Returns the full list of categories — no pagination needed, small fixed list.")
                    .responses(new ApiResponses()
                            .addApiResponse("200", arrayResponse("Category list", CategoryDto.class))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));
            paths.addPathItem("/api/categories", categoriesPath);

            // ── PUT /api/categories/{id} ──────────────────────────────────
            PathItem categoryByIdPath = paths.getOrDefault("/api/categories/{id}", new PathItem());
            categoryByIdPath.setPut(new Operation()
                    .addTagsItem("Categories")
                    .summary("Update a category name")
                    .description("""
                            Updates the name of an existing category.
                            Returns 404 if the category does not exist.
                            Returns 409 if the new name collides with an existing category name.
                            Name comparison is case-insensitive.
                            """)
                    .addParametersItem(new Parameter()
                            .name("id")
                            .in("path")
                            .required(true)
                            .description("Category ID")
                            .schema(new Schema<>().type("integer")))
                    .addParametersItem(new Parameter()
                            .name("name")
                            .in("query")
                            .required(true)
                            .description("New category name (max 100 chars, must not be blank)")
                            .schema(new Schema<>().type("string").maxLength(100)))
                    .responses(new ApiResponses()
                            .addApiResponse("200", jsonResponse("Category updated", CategoryDto.class))
                            .addApiResponse("400", messageResponse("name is blank or exceeds 100 characters"))
                            .addApiResponse("404", messageResponse("Category not found"))
                            .addApiResponse("409", messageResponse("Category name already exists"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));

            // ── DELETE /api/categories/{id} ───────────────────────────────
            categoryByIdPath.setDelete(new Operation()
                    .addTagsItem("Categories")
                    .summary("Delete a category")
                    .description("""
                            Deletes a category by ID.
                            Returns 204 on success.
                            Returns 404 if the category does not exist.
                            Returns 409 if the category has expenses attached (ON DELETE RESTRICT).
                            The raw DB constraint is caught and returned as a clean error message.
                            """)
                    .addParametersItem(new Parameter()
                            .name("id")
                            .in("path")
                            .required(true)
                            .description("Category ID")
                            .schema(new Schema<>().type("integer")))
                    .responses(new ApiResponses()
                            .addApiResponse("204", messageResponse("Category deleted"))
                            .addApiResponse("404", messageResponse("Category not found"))
                            .addApiResponse("409", messageResponse("Category has expenses attached and cannot be deleted"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));

            paths.addPathItem("/api/categories/{id}", categoryByIdPath);

            // ── GET /api/user ─────────────────────────────────────────────
            PathItem usersPath = paths.getOrDefault("/api/user", new PathItem());
            usersPath.setGet(new Operation()
                    .addTagsItem("Users")
                    .summary("List all users")
                    .description("Returns a paginated list of users.")
                    .addParametersItem(queryParam("page", "integer", "Page number (0-based)", false))
                    .addParametersItem(queryParam("size", "integer", "Page size", false))
                    .responses(new ApiResponses()
                            .addApiResponse("200", messageResponse("Paginated user list"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));

            // ── POST /api/user ────────────────────────────────────────────
            usersPath.setPost(new Operation()
                    .addTagsItem("Users")
                    .summary("Create a new user")
                    .description("Creates a user. username and email must be unique.")
                    .requestBody(jsonRequestBody(UserDto.class))
                    .responses(new ApiResponses()
                            .addApiResponse("201", jsonResponse("User created", UserDto.class))
                            .addApiResponse("400", messageResponse("Validation failed"))
                            .addApiResponse("409", messageResponse("username or email already exists"))
                            .addApiResponse("500", messageResponse("Unexpected server error"))));
            paths.addPathItem("/api/user", usersPath);
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private RequestBody jsonRequestBody(Class<?> schemaClass) {
        return new RequestBody()
                .required(true)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref(schemaClass.getSimpleName()))));
    }

    private ApiResponse jsonResponse(String description, Class<?> schemaClass) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref(schemaClass.getSimpleName()))));
    }

    private ApiResponse arrayResponse(String description, Class<?> schemaClass) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new ArraySchema()
                                .items(new Schema<>().$ref(schemaClass.getSimpleName())))));
    }

    private ApiResponse messageResponse(String description) {
        return new ApiResponse().description(description);
    }

    private Parameter queryParam(String name, String type, String description, boolean required) {
        return new Parameter()
                .name(name)
                .in("query")
                .required(required)
                .description(description)
                .schema(new Schema<>().type(type));
    }


}