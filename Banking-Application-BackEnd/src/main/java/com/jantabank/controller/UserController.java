package com.jantabank.controller;

import com.jantabank.dto.UserDto;
import com.jantabank.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    //Build get all account REST Api
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUserRequests()
    {
        List<UserDto> users =  userService.getAllUserRequests();

        List<UserDto> usersWithoutPasswords = users.stream()
                .map(user -> {
                    user.setPassword(null);
                    return user;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersWithoutPasswords);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<UserDto> rejectRequest(@PathVariable("id") Long userid)
    {
        UserDto user =  userService.rejectRequest(userid);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("{id}")
    public ResponseEntity<UserDto> generateAccount(@PathVariable("id") Long userid)
    {
        UserDto user =  userService.generateAccount(userid);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }


}
