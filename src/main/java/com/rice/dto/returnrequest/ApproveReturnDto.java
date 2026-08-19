package com.rice.dto.returnrequest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ApproveReturnDto {
    @NotEmpty(message = "Admin note is required")
    private String adminNote;

    private String returnInstructions;
}
