package com.jantabank.entity;

import com.jantabank.domain.enums.TicketCategory;
import com.jantabank.domain.enums.TicketPriority;
import com.jantabank.domain.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "support_tickets")
public class SupportTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_reference", length = 40, nullable = false, unique = true)
    private String ticketReference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private TicketCategory category;

    @Column(name = "subject", length = 200, nullable = false)
    private String subject;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TicketStatus status;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
