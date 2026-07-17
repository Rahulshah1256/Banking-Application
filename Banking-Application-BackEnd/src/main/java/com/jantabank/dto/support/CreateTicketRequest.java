package com.jantabank.dto.support;

import com.jantabank.domain.enums.TicketCategory;
import com.jantabank.domain.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotNull(message = "Category is required")
    private TicketCategory category;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be at most 200 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private TicketPriority priority;
}
