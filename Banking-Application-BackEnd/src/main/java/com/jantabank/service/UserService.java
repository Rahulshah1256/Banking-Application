package com.jantabank.service;
import com.jantabank.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUserRequests();

    UserDto rejectRequest(long userid);

    UserDto generateAccount(long userid);
}
