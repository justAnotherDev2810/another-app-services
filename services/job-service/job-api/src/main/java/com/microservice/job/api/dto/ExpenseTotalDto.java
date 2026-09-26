package com.microservice.job.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Aggregate total for filtered expenses")
public class ExpenseTotalDto {

    @Schema(description = "Sum of all matched expense amounts", example = "1450.00")
    private BigDecimal total;

    @Schema(description = "Number of matched expenses", example = "12")
    private Long count;
}