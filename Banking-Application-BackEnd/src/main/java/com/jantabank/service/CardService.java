package com.jantabank.service;

import com.jantabank.dto.card.BlockCardRequest;
import com.jantabank.dto.card.CardControlsRequest;
import com.jantabank.dto.card.CardDto;
import com.jantabank.dto.card.CardHistoryDto;
import com.jantabank.dto.card.CardLimitsRequest;
import com.jantabank.dto.card.IssueCardRequest;
import com.jantabank.dto.card.SetPinRequest;

import java.util.List;

/**
 * Debit-card lifecycle and control operations, scoped to the authenticated
 * customer's own cards.
 */
public interface CardService {

    CardDto issue(IssueCardRequest request, String username);

    List<CardDto> listMine(String username);

    CardDto get(long id, String username);

    CardDto block(long id, BlockCardRequest request, String username);

    CardDto unblock(long id, String username);

    CardDto replace(long id, String username);

    CardDto setPin(long id, SetPinRequest request, String username);

    CardDto updateControls(long id, CardControlsRequest request, String username);

    CardDto updateLimits(long id, CardLimitsRequest request, String username);

    List<CardHistoryDto> history(long id, String username);
}
