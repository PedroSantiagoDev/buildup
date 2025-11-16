package com.maistech.buildup.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request to add a member to a project")
public record AddMemberRequest(
    @Schema(description = "ID of the user to add as member", required = true)
    @NotNull(message = "User ID is required") 
    UUID userId,

    @Schema(description = "Role of the member in the project", example = "Developer", required = true)
    @NotEmpty(message = "Role is required") 
    String role,

    @Schema(description = "Whether the member can edit the project", example = "true", defaultValue = "false")
    Boolean canEdit
) {
    public AddMemberRequest {
        if (canEdit == null) {
            canEdit = false;
        }
    }
}
