package com.lidcoin.reserveFound.domain.enums;

public enum TransactionType {
    /**
     * Dépôt dans la réserve
     */
    DEPOSIT("Deposit", "Deposit to reserve fund"),

    /**
     * Retrait de la réserve
     */
    WITHDRAWAL("Withdrawal", "Withdrawal from reserve fund"),

    /**
     * Transfert interne
     */
    TRANSFER("Transfer", "Internal transfer"),

    /**
     * Allocation de réserve
     */
    ALLOCATION("Allocation", "Reserve allocation"),

    /**
     * Ajustement de réserve
     */
    ADJUSTMENT("Adjustment", "Reserve adjustment"),

    /**
     * Intérêts générés
     */
    INTEREST("Interest", "Interest generated"),

    /**
     * Frais de gestion
     */
    FEE("Fee", "Management fee"),

    /**
     * Remboursement
     */
    REFUND("Refund", "Refund transaction");

    private final String displayName;
    private final String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCredit() {
        return this == DEPOSIT || this == INTEREST || this == REFUND;
    }

    public boolean isDebit() {
        return this == WITHDRAWAL || this == FEE;
    }
}