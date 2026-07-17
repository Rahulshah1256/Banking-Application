package com.jantabank.impl;

import com.jantabank.dto.profile.NomineeRequest;
import com.jantabank.dto.profile.NomineeResponse;
import com.jantabank.entity.Nominee;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.NomineeRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.NomineeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NomineeServiceImpl implements NomineeService {

    private static final Logger log = LoggerFactory.getLogger(NomineeServiceImpl.class);

    private final NomineeRepository nomineeRepository;
    private final UserRepository userRepository;

    public NomineeServiceImpl(NomineeRepository nomineeRepository, UserRepository userRepository) {
        this.nomineeRepository = nomineeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public NomineeResponse add(NomineeRequest request, String username) {
        User user = loadUser(username);
        assertShareWithinLimit(user.getId(), null, request.getSharePercentage());

        Nominee nominee = new Nominee();
        nominee.setUserId(user.getId());
        apply(nominee, request);
        nominee = nomineeRepository.save(nominee);
        log.info("Nominee {} added for user {}", nominee.getId(), user.getId());
        return toResponse(nominee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NomineeResponse> listMine(String username) {
        User user = loadUser(username);
        return nomineeRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NomineeResponse update(Long id, NomineeRequest request, String username) {
        User user = loadUser(username);
        Nominee nominee = nomineeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nominee not found"));
        assertShareWithinLimit(user.getId(), id, request.getSharePercentage());
        apply(nominee, request);
        nomineeRepository.save(nominee);
        log.info("Nominee {} updated for user {}", id, user.getId());
        return toResponse(nominee);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        User user = loadUser(username);
        Nominee nominee = nomineeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nominee not found"));
        nomineeRepository.delete(nominee);
        log.info("Nominee {} deleted for user {}", id, user.getId());
    }

    private void assertShareWithinLimit(Long userId, Long excludeId, double newShare) {
        double existing = nomineeRepository.findByUserIdOrderByIdAsc(userId).stream()
                .filter(n -> excludeId == null || !n.getId().equals(excludeId))
                .mapToDouble(Nominee::getSharePercentage)
                .sum();
        if (existing + newShare > 100.0) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,
                    "Total nominee share cannot exceed 100%. Currently allocated: " + existing + "%");
        }
    }

    private void apply(Nominee nominee, NomineeRequest request) {
        nominee.setName(request.getName().trim());
        nominee.setRelationship(request.getRelationship().trim());
        nominee.setSharePercentage(request.getSharePercentage());
        nominee.setDateOfBirth(request.getDateOfBirth());
        nominee.setPhone(request.getPhone());
        nominee.setAddress(request.getAddress());
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NomineeResponse toResponse(Nominee n) {
        return NomineeResponse.builder()
                .id(n.getId())
                .name(n.getName())
                .relationship(n.getRelationship())
                .sharePercentage(n.getSharePercentage())
                .dateOfBirth(n.getDateOfBirth())
                .phone(n.getPhone())
                .address(n.getAddress())
                .build();
    }
}
