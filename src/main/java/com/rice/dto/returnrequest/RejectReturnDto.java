package com.rice.dto.returnrequest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RejectReturnDto {
    @NotEmpty(message = "Reason is required")
    private String reason;

    @NotEmpty(message = "Admin note is required")
    private String adminNote;
}
