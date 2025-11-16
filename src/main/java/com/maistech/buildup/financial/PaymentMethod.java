package com.maistech.buildup.financial;

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

    public String getDisplayName() {
        return displayName;
    }
}
