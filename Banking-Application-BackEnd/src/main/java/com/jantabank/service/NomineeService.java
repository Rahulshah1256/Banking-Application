package com.jantabank.service;

import com.jantabank.dto.profile.NomineeRequest;
import com.jantabank.dto.profile.NomineeResponse;

import java.util.List;

public interface NomineeService {

    NomineeResponse add(NomineeRequest request, String username);

    List<NomineeResponse> listMine(String username);

    NomineeResponse update(Long id, NomineeRequest request, String username);

    void delete(Long id, String username);
}
