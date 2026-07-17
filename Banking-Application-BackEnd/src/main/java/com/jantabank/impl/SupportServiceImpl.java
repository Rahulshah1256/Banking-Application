package com.jantabank.impl;

import com.jantabank.domain.enums.MessageSenderType;
import com.jantabank.domain.enums.NotificationChannel;
import com.jantabank.domain.enums.NotificationType;
import com.jantabank.domain.enums.TicketPriority;
import com.jantabank.domain.enums.TicketStatus;
import com.jantabank.dto.support.BranchLocatorResponse;
import com.jantabank.dto.support.CreateTicketRequest;
import com.jantabank.dto.support.FaqResponse;
import com.jantabank.dto.support.TicketMessageRequest;
import com.jantabank.dto.support.TicketMessageResponse;
import com.jantabank.dto.support.TicketResponse;
import com.jantabank.dto.support.UpdateTicketStatusRequest;
import com.jantabank.entity.SupportTicket;
import com.jantabank.entity.TicketMessage;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.SupportTicketRepository;
import com.jantabank.repository.TicketMessageRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.NotificationService;
import com.jantabank.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter REF_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private final List<FaqResponse> faqSeed = buildFaqs();
    private final List<BranchLocatorResponse> locationSeed = buildLocations();

    public SupportServiceImpl(SupportTicketRepository ticketRepository,
                              TicketMessageRepository messageRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TicketResponse raiseTicket(CreateTicketRequest request, String username) {
        User user = loadUser(username);
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketReference(generateReference());
        ticket.setUserId(user.getId());
        ticket.setCategory(request.getCategory());
        ticket.setSubject(request.getSubject().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPriority(request.getPriority() == null ? TicketPriority.MEDIUM : request.getPriority());
        ticket.setStatus(TicketStatus.OPEN);
        SupportTicket saved = ticketRepository.save(ticket);

        TicketMessage first = new TicketMessage();
        first.setTicketId(saved.getId());
        first.setSenderType(MessageSenderType.CUSTOMER);
        first.setSenderUserId(user.getId());
        first.setMessage(request.getDescription().trim());
        messageRepository.save(first);

        log.info("Support ticket raised ref={} userId={} category={}", saved.getTicketReference(),
                user.getId(), saved.getCategory());

        safeNotify(user.getId(), NotificationType.GENERAL,
                "Ticket " + saved.getTicketReference() + " created",
                "We have received your request regarding \"" + saved.getSubject()
                        + "\". Our team will get back to you shortly.");

        return toResponse(saved, messageRepository.findByTicketIdOrderByIdAsc(saved.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> listMine(String username) {
        User user = loadUser(username);
        return ticketRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(t -> toResponse(t, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id, String username, boolean admin) {
        SupportTicket ticket = loadTicket(id, username, admin);
        return toResponse(ticket, messageRepository.findByTicketIdOrderByIdAsc(ticket.getId()));
    }

    @Override
    @Transactional
    public TicketResponse addMessage(Long id, TicketMessageRequest request, String username, boolean admin) {
        SupportTicket ticket = loadTicket(id, username, admin);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Cannot add messages to a closed ticket");
        }

        User user = loadUser(username);
        TicketMessage message = new TicketMessage();
        message.setTicketId(ticket.getId());
        message.setSenderType(admin ? MessageSenderType.AGENT : MessageSenderType.CUSTOMER);
        message.setSenderUserId(user.getId());
        message.setMessage(request.getMessage().trim());
        messageRepository.save(message);

        if (admin && ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticketRepository.save(ticket);

        if (admin) {
            safeNotify(ticket.getUserId(), NotificationType.GENERAL,
                    "New reply on ticket " + ticket.getTicketReference(),
                    "Our support team has replied to your ticket \"" + ticket.getSubject() + "\".");
        }

        return toResponse(ticket, messageRepository.findByTicketIdOrderByIdAsc(ticket.getId()));
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        TicketStatus newStatus = request.getStatus();
        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        } else {
            ticket.setResolvedAt(null);
        }
        ticketRepository.save(ticket);

        safeNotify(ticket.getUserId(), NotificationType.GENERAL,
                "Ticket " + ticket.getTicketReference() + " " + newStatus.name().toLowerCase().replace('_', ' '),
                "The status of your ticket \"" + ticket.getSubject() + "\" is now "
                        + newStatus.name().replace('_', ' ') + ".");

        log.info("Support ticket ref={} status updated to {}", ticket.getTicketReference(), newStatus);
        return toResponse(ticket, messageRepository.findByTicketIdOrderByIdAsc(ticket.getId()));
    }

    @Override
    public List<FaqResponse> faqs(String category) {
        if (category == null || category.isBlank()) {
            return faqSeed;
        }
        String c = category.trim();
        return faqSeed.stream()
                .filter(f -> f.getCategory().equalsIgnoreCase(c))
                .toList();
    }

    @Override
    public List<BranchLocatorResponse> branches(String city) {
        return filterLocations("BRANCH", city);
    }

    @Override
    public List<BranchLocatorResponse> atms(String city) {
        return filterLocations("ATM", city);
    }

    private List<BranchLocatorResponse> filterLocations(String type, String city) {
        return locationSeed.stream()
                .filter(l -> l.getType().equalsIgnoreCase(type))
                .filter(l -> city == null || city.isBlank() || l.getCity().equalsIgnoreCase(city.trim()))
                .toList();
    }

    private void safeNotify(Long userId, NotificationType type, String title, String message) {
        try {
            notificationService.notify(userId, type, NotificationChannel.IN_APP, title, message);
        } catch (Exception ex) {
            log.warn("Failed to create support notification for userId={}: {}", userId, ex.getMessage());
        }
    }

    private SupportTicket loadTicket(Long id, String username, boolean admin) {
        if (admin) {
            return ticketRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        }
        User user = loadUser(username);
        return ticketRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private String generateReference() {
        for (int attempt = 0; attempt < 6; attempt++) {
            String ref = "TKT" + LocalDateTime.now().format(REF_FMT)
                    + String.format("%05d", RANDOM.nextInt(100_000));
            if (!ticketRepository.existsByTicketReference(ref)) {
                return ref;
            }
        }
        throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to allocate ticket reference");
    }

    private TicketResponse toResponse(SupportTicket ticket, List<TicketMessage> messages) {
        List<TicketMessageResponse> messageResponses = messages == null ? null
                : messages.stream().map(this::toMessageResponse).toList();
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketReference(ticket.getTicketReference())
                .category(ticket.getCategory())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .messages(messageResponses)
                .build();
    }

    private TicketMessageResponse toMessageResponse(TicketMessage m) {
        return TicketMessageResponse.builder()
                .id(m.getId())
                .senderType(m.getSenderType())
                .senderUserId(m.getSenderUserId())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<FaqResponse> buildFaqs() {
        List<FaqResponse> list = new ArrayList<>();
        list.add(FaqResponse.builder().category("ACCOUNT")
                .question("How do I check my account balance?")
                .answer("Log in to NetBanking and open the Dashboard, or visit Accounts to view balances for all your accounts.")
                .build());
        list.add(FaqResponse.builder().category("ACCOUNT")
                .question("How do I update my registered mobile number?")
                .answer("Raise a support ticket under the ACCOUNT category or visit your nearest branch with a valid ID proof.")
                .build());
        list.add(FaqResponse.builder().category("CARD")
                .question("How do I block a lost or stolen card?")
                .answer("Go to Cards, select the affected card and choose Block. You can request a replacement immediately after blocking.")
                .build());
        list.add(FaqResponse.builder().category("CARD")
                .question("How can I change my card PIN?")
                .answer("Open Cards, select the card and use the Set/Reset PIN option after OTP verification.")
                .build());
        list.add(FaqResponse.builder().category("TRANSACTION")
                .question("What is the daily fund transfer limit?")
                .answer("Limits depend on the transfer channel (IMPS/NEFT/UPI). You can view and request limit changes from the Transfers screen.")
                .build());
        list.add(FaqResponse.builder().category("TRANSACTION")
                .question("A transfer failed but money was debited. What should I do?")
                .answer("Failed transfers are usually auto-reversed within 24 hours. If not, raise a ticket under the TRANSACTION category with the reference number.")
                .build());
        list.add(FaqResponse.builder().category("LOAN")
                .question("How is my EMI calculated?")
                .answer("EMI is computed on reducing balance using the formula P*r*(1+r)^n/((1+r)^n-1). Use the Loan calculator for an estimate.")
                .build());
        list.add(FaqResponse.builder().category("KYC")
                .question("How long does KYC verification take?")
                .answer("Uploaded KYC documents are typically reviewed within 2 working days. You will be notified once verified.")
                .build());
        list.add(FaqResponse.builder().category("GENERAL")
                .question("How do I contact customer support?")
                .answer("Raise a support ticket from the Support section, or call our 24x7 helpline. You can track ticket status in Support > My Tickets.")
                .build());
        return List.copyOf(list);
    }

    private List<BranchLocatorResponse> buildLocations() {
        List<BranchLocatorResponse> list = new ArrayList<>();
        list.add(BranchLocatorResponse.builder().name("Janta Bank - Fort Branch").type("BRANCH")
                .address("12 Dalal Street, Fort").city("Mumbai").state("Maharashtra").pincode("400001")
                .ifsc("JANB0000001").phone("022-40000001").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank - Andheri Branch").type("BRANCH")
                .address("45 Link Road, Andheri West").city("Mumbai").state("Maharashtra").pincode("400053")
                .ifsc("JANB0000002").phone("022-40000002").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank - Connaught Place Branch").type("BRANCH")
                .address("7 Connaught Place").city("New Delhi").state("Delhi").pincode("110001")
                .ifsc("JANB0000003").phone("011-40000003").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank - Koramangala Branch").type("BRANCH")
                .address("80 Feet Road, Koramangala").city("Bengaluru").state("Karnataka").pincode("560095")
                .ifsc("JANB0000004").phone("080-40000004").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank ATM - CST").type("ATM")
                .address("CST Station Concourse").city("Mumbai").state("Maharashtra").pincode("400001")
                .ifsc("JANB0000001").phone("1800-000-000").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank ATM - Andheri Metro").type("ATM")
                .address("Andheri Metro Station").city("Mumbai").state("Maharashtra").pincode("400053")
                .ifsc("JANB0000002").phone("1800-000-000").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank ATM - Rajiv Chowk").type("ATM")
                .address("Rajiv Chowk Metro").city("New Delhi").state("Delhi").pincode("110001")
                .ifsc("JANB0000003").phone("1800-000-000").build());
        list.add(BranchLocatorResponse.builder().name("Janta Bank ATM - Forum Mall").type("ATM")
                .address("Forum Mall, Koramangala").city("Bengaluru").state("Karnataka").pincode("560095")
                .ifsc("JANB0000004").phone("1800-000-000").build());
        return List.copyOf(list);
    }
}
