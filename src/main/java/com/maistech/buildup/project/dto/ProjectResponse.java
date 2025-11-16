package com.maistech.buildup.project.dto;

import com.maistech.buildup.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Project details including financial data and calculated fields")
public record ProjectResponse(
    @Schema(description = "Project unique identifier")
    UUID id,
    
    @Schema(description = "Project name", example = "Shopping Mall Construction")
    String name,
    
    @Schema(description = "Client name", example = "ABC Corporation")
    String clientName,
    
    @Schema(description = "Project description")
    String description,
    
    @Schema(description = "Project start date")
    LocalDate startDate,
    
    @Schema(description = "Project due/deadline date")
    LocalDate dueDate,
    
    @Schema(description = "Total contract value", example = "1500000.00")
    BigDecimal contractValue,
    
    @Schema(description = "Down payment amount", example = "450000.00")
    BigDecimal downPayment,
    
    @Schema(description = "Remaining payment (calculated as contractValue - downPayment)", example = "1050000.00")
    BigDecimal remainingPayment,
    
    @Schema(description = "URL to project cover image")
    String coverImageUrl,
    
    @Schema(description = "Current project status")
    ProjectStatus status,
    
    @Schema(description = "Whether the project is overdue (past due date and still in progress)")
    boolean isOverdue,
    
    @Schema(description = "Number of days until due date (negative if overdue)", example = "45")
    Long daysUntilDueDate,
    
    @Schema(description = "ID of the user who created the project")
    UUID createdById,
    
    @Schema(description = "Name of the user who created the project")
    String createdByName,
    
    @Schema(description = "Timestamp when the project was created")
    LocalDateTime createdAt,
    
    @Schema(description = "List of project members")
    List<ProjectMemberResponse> members
) {}
