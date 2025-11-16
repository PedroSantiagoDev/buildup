package com.maistech.buildup.project.dto;

import com.maistech.buildup.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to update project information. All fields are optional - only provided fields will be updated.")
public record UpdateProjectRequest(
    @Schema(description = "Project name", example = "Shopping Mall Construction - Phase 2")
    String name,
    
    @Schema(description = "Client name", example = "ABC Corporation")
    String clientName,
    
    @Schema(description = "Project description")
    String description,
    
    @Schema(description = "Project start date")
    LocalDate startDate,
    
    @Schema(description = "Project due/deadline date")
    LocalDate dueDate,
    
    @Schema(description = "Total contract value", example = "1800000.00")
    BigDecimal contractValue,
    
    @Schema(description = "Down payment amount", example = "540000.00")
    BigDecimal downPayment,
    
    @Schema(description = "URL to project cover image")
    String coverImageUrl,
    
    @Schema(description = "Project status")
    ProjectStatus status
) {}
