package com.rice.dto.returnrequest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReceiveReturnDto {
    @NotEmpty(message = "Received condition is required")
    private String receivedCondition;

    private String receivedNote;
}
