package com.maistech.buildup.financial;

public enum MilestoneStatus {
    PENDING("Pendente"),
    PAID("Pago"),
    LATE("Atrasado"),
    CANCELLED("Cancelado");

    private final String displayName;

    MilestoneStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
