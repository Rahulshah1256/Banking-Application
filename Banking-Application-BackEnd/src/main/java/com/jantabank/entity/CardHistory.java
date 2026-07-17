package com.jantabank.entity;

import com.jantabank.domain.enums.CardAction;
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

/**
 * Immutable audit record of an action performed on a card.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "card_history")
public class CardHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 30, nullable = false)
    private CardAction action;

    @Column(name = "details", length = 255)
    private String details;
}
