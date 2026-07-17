package com.jantabank.dto.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchLocatorResponse {
    private String name;
    private String type;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String ifsc;
    private String phone;
}
