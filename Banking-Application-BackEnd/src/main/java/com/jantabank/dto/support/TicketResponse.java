package com.jantabank.dto.support;

import com.jantabank.domain.enums.TicketCategory;
import com.jantabank.domain.enums.TicketPriority;
import com.jantabank.domain.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String ticketReference;
    private TicketCategory category;
    private String subject;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketMessageResponse> messages;
}
