package com.jantabank.domain.enums;

public enum CardNetwork {
    VISA("4"),
    MASTERCARD("5"),
    RUPAY("6");

    private final String prefix;

    CardNetwork(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
