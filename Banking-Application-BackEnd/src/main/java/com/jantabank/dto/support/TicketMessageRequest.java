package com.jantabank.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketMessageRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    private String message;
}
