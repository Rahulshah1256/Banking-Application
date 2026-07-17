package com.jantabank.dto.card;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read model for a card action-history entry.
 */
@Getter
@Setter
@Builder
public class CardHistoryDto {

    private Long id;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
