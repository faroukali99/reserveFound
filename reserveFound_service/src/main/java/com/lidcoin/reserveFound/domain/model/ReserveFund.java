package com.lidcoin.reserveFound.domain.model;

import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reserve_funds")
public class ReserveFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

   @NotNull(message = "Le solde est obligatoire")
    @DecimalMin(value = "0.0", message = "Le solde ne peut pas être négatif")
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull(message = "La devise est obligatoire")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "XOF"; // Franc CFA

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FundStatus status = FundStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reference", unique = true, nullable = false, length = 100)
    private String reference;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Informations sur la source/destination
    @Column(name = "source_account", length = 100)
    private String sourceAccount;

    @Column(name = "destination_account", length = 100)
    private String destinationAccount;

    // Métadonnées pour l'audit
    @Column(name = "transaction_hash", length = 256)
    private String transactionHash;

    @Column(name = "block_number")
    private Long blockNumber;

    // Constructeurs
    public ReserveFund() {}

    public ReserveFund(BigDecimal amount, String currency, TransactionType transactionType) {
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.balance = BigDecimal.ZERO;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public FundStatus getStatus() { return status; }
    public void setStatus(FundStatus status) { this.status = status; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }



    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }

    public String getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(String destinationAccount) { this.destinationAccount = destinationAccount; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }

    // Méthodes utilitaires
    public void addToBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedDate = LocalDateTime.now();
    }

    public void subtractFromBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (this.reference == null || this.reference.isEmpty()) {
            this.reference = "RF-" + UUID.randomUUID().toString().toUpperCase().substring(0, 12);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}