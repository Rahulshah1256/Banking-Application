package com.jantabank.dto.support;

import com.jantabank.domain.enums.MessageSenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageResponse {
    private Long id;
    private MessageSenderType senderType;
    private Long senderUserId;
    private String message;
    private LocalDateTime createdAt;
}
