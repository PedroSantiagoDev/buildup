package com.maistech.buildup.financial;

public enum ExpenseStatus {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
