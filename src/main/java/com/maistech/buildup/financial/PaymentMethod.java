package com.maistech.buildup.financial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    DINHEIRO("Dinheiro"),
    PIX("PIX"),
    CARTAO_CREDITO("Cartão de Crédito"),
    CARTAO_DEBITO("Cartão de Débito"),
    BOLETO("Boleto"),
    TRANSFERENCIA("Transferência Bancária"),
    CHEQUE("Cheque");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static PaymentMethod fromString(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.displayName.equals(value) || method.name().equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMethod: " + value);
    }
}
