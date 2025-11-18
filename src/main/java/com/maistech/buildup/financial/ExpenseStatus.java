package com.maistech.buildup.financial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpenseStatus {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ExpenseStatus fromString(String value) {
        for (ExpenseStatus status : ExpenseStatus.values()) {
            if (status.displayName.equals(value) || status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ExpenseStatus: " + value);
    }
}
