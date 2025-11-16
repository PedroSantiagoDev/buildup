package com.maistech.buildup.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Project member information")
public record ProjectMemberResponse(
    @Schema(description = "Member record unique identifier")
    UUID id,
    
    @Schema(description = "User ID")
    UUID userId,
    
    @Schema(description = "User name", example = "John Doe")
    String userName,
    
    @Schema(description = "User email", example = "john.doe@example.com")
    String userEmail,
    
    @Schema(description = "Member role in the project", example = "Developer")
    String role,
    
    @Schema(description = "Whether the member can edit the project")
    Boolean canEdit,
    
    @Schema(description = "Timestamp when the member joined the project")
    LocalDateTime joinedAt
) {}
