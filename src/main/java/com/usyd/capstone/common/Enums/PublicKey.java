package com.usyd.capstone.common.Enums;

public enum PublicKey {
    firstKey("CS76-2");

    private final String value;

    PublicKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
