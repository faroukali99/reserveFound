package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.application.dto.ReserveFundDTO;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.excption.InvalidTransactionException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TransactionValidationService {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000000.00"); // 10M XOF
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("100.00"); // 100 XOF
    private static final int MAX_DAILY_TRANSACTIONS = 50;
    private static final BigDecimal MAX_DAILY_AMOUNT = new BigDecimal("50000000.00"); // 50M XOF

    public void validateTransaction(ReserveFundDTO dto) {
        validateAmount(dto.getAmount());
        validateTransactionType(dto.getTransactionType());
        validateCurrency(dto.getCurrency());
        validateDescription(dto.getDescription());
    }

    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidTransactionException("Le montant ne peut pas être null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Le montant doit être positif");
        }

        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new InvalidTransactionException(
                    String.format("Le montant minimum est de %s XOF", MIN_TRANSACTION_AMOUNT)
            );
        }

        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                    String.format("Le montant maximum par transaction est de %s XOF", MAX_TRANSACTION_AMOUNT)
            );
        }

        // Vérifier le nombre de décimales (max 2)
        if (amount.scale() > 2) {
            throw new InvalidTransactionException("Le montant ne peut avoir que 2 décimales maximum");
        }
    }

    public void validateTransactionType(TransactionType type) {
        if (type == null) {
            throw new InvalidTransactionException("Le type de transaction est obligatoire");
        }
    }

    public void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new InvalidTransactionException("La devise est obligatoire");
        }

        if (!currency.equals("XOF")) {
            throw new InvalidTransactionException("Seule la devise XOF est supportée actuellement");
        }
    }

    public void validateDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new InvalidTransactionException("La description ne peut pas dépasser 500 caractères");
        }
    }

    public void validateDailyLimit(Long userId, BigDecimal amount, int transactionCount) {
        if (transactionCount >= MAX_DAILY_TRANSACTIONS) {
            throw new InvalidTransactionException(
                    String.format("Limite quotidienne de %d transactions atteinte", MAX_DAILY_TRANSACTIONS)
            );
        }

        if (amount.compareTo(MAX_DAILY_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                    String.format("Limite quotidienne de %s XOF atteinte", MAX_DAILY_AMOUNT)
            );
        }
    }

    public void validateWithdrawal(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InvalidTransactionException(
                    String.format("Solde insuffisant. Disponible: %s XOF, Demandé: %s XOF", balance, amount)
            );
        }
    }

    public void validateTransfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId == null || toUserId == null) {
            throw new InvalidTransactionException("Les identifiants des utilisateurs sont obligatoires");
        }

        if (fromUserId.equals(toUserId)) {
            throw new InvalidTransactionException("Impossible de transférer vers le même compte");
        }

        validateAmount(amount);
    }

    public boolean isHighRiskTransaction(BigDecimal amount, TransactionType type) {
        // Transactions supérieures à 5M XOF sont à haut risque
        BigDecimal highRiskThreshold = new BigDecimal("5000000.00");

        return amount.compareTo(highRiskThreshold) > 0 ||
                type == TransactionType.WITHDRAWAL;
    }

    public boolean requiresAdditionalVerification(BigDecimal amount, Long userId) {
        // Transactions supérieures à 1M XOF nécessitent une vérification supplémentaire
        BigDecimal verificationThreshold = new BigDecimal("1000000.00");
        return amount.compareTo(verificationThreshold) > 0;
    }

    public void validateTransactionTiming(LocalDateTime lastTransaction) {
        if (lastTransaction != null) {
            long minutesSinceLastTransaction = ChronoUnit.MINUTES.between(lastTransaction, LocalDateTime.now());

            // Limite de débit: pas plus d'une transaction par minute
            if (minutesSinceLastTransaction < 1) {
                throw new InvalidTransactionException(
                        "Veuillez attendre au moins 1 minute entre deux transactions"
                );
            }
        }
    }

    public BigDecimal calculateTransactionFee(BigDecimal amount, TransactionType type) {
        // Frais de transaction: 0.5% pour les retraits, gratuit pour les dépôts
        if (type == TransactionType.WITHDRAWAL) {
            BigDecimal feeRate = new BigDecimal("0.005");
            BigDecimal fee = amount.multiply(feeRate);

            // Frais minimum de 100 XOF
            BigDecimal minFee = new BigDecimal("100.00");
            return fee.max(minFee);
        }

        return BigDecimal.ZERO;
    }
}