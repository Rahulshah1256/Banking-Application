package com.jantabank.dto.cheque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopChequeRequest {

    @NotBlank(message = "Stop reason is required")
    @Size(max = 255, message = "Reason is too long")
    private String reason;
}
