package com.jantabank.dto.support;

import com.jantabank.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;
}
