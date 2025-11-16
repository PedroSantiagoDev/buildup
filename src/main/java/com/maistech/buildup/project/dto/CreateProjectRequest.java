package com.maistech.buildup.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to create a new construction project")
public record CreateProjectRequest(
    @Schema(description = "Project name", example = "Shopping Mall Construction", required = true)
    @NotEmpty(message = "Project name is required") 
    String name,

    @Schema(description = "Client name", example = "ABC Corporation")
    String clientName,
    
    @Schema(description = "Project description", example = "Construction of a 3-story shopping mall with parking")
    String description,
    
    @Schema(description = "Project start date", example = "2024-01-01")
    LocalDate startDate,
    
    @Schema(description = "Project due/deadline date", example = "2024-12-31")
    LocalDate dueDate,

    @Schema(description = "Total contract value", example = "1500000.00")
    @Positive(message = "Contract value must be positive")
    BigDecimal contractValue,

    @Schema(description = "Down payment amount", example = "450000.00")
    BigDecimal downPayment,
    
    @Schema(description = "URL to project cover image", example = "https://example.com/images/project-cover.jpg")
    String coverImageUrl
) {}
