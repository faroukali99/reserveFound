package com.lidcoin.reserveFound.domain.enums;

public enum FundStatus {
    /**
     * Fonds actif et opérationnel
     */
    ACTIVE("Active", "Fund is active and operational"),

    /**
     * Fonds en attente de validation
     */
    PENDING("Pending", "Fund transaction is pending validation"),

    /**
     * Fonds gelé temporairement
     */
    FROZEN("Frozen", "Fund is temporarily frozen"),

    /**
     * Fonds bloqué
     */
    BLOCKED("Blocked", "Fund is blocked due to security reasons"),

    /**
     * Transaction complétée
     */
    COMPLETED("Completed", "Transaction has been completed"),

    /**
     * Transaction échouée
     */
    FAILED("Failed", "Transaction has failed"),

    /**
     * Transaction annulée
     */
    CANCELLED("Cancelled", "Transaction has been cancelled");

    private final String displayName;
    private final String description;

    FundStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canBeModified() {
        return this == ACTIVE || this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}