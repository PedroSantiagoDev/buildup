package com.maistech.buildup.financial;

public enum ExpenseStatus {
    PENDING("Pendente"),
    PAID("Pago"),
    OVERDUE("Vencido"),
    CANCELLED("Cancelado");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
