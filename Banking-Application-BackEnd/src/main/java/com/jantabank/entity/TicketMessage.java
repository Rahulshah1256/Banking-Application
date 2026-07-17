package com.jantabank.entity;

import com.jantabank.domain.enums.MessageSenderType;
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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ticket_messages")
public class TicketMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 20, nullable = false)
    private MessageSenderType senderType;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "message", length = 2000, nullable = false)
    private String message;
}
