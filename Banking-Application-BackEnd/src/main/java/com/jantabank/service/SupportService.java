package com.jantabank.service;

import com.jantabank.dto.support.BranchLocatorResponse;
import com.jantabank.dto.support.CreateTicketRequest;
import com.jantabank.dto.support.FaqResponse;
import com.jantabank.dto.support.TicketMessageRequest;
import com.jantabank.dto.support.TicketResponse;
import com.jantabank.dto.support.UpdateTicketStatusRequest;

import java.util.List;

public interface SupportService {

    TicketResponse raiseTicket(CreateTicketRequest request, String username);

    List<TicketResponse> listMine(String username);

    TicketResponse getTicket(Long id, String username, boolean admin);

    TicketResponse addMessage(Long id, TicketMessageRequest request, String username, boolean admin);

    TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request);

    List<FaqResponse> faqs(String category);

    List<BranchLocatorResponse> branches(String city);

    List<BranchLocatorResponse> atms(String city);
}
