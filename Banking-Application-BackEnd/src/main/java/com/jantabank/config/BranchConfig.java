package com.jantabank.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Getter
public class BranchConfig {
    @Value("${bank.branch-id}")
    public String branchId;

    @Value("${bank.branch-ifsc}")
    public String branchIfsc;

    @Value("${bank.branch-name}")
    public String branchname;

}
