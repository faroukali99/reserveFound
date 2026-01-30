package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import com.lidcoin.reserveFound.infrastructure.repository.ReserveFundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionService {

    @Autowired
    private ReserveFundRepository reserveFundRepository;

    private static final int SUSPICIOUS_TRANSACTION_COUNT = 10;
    private static final BigDecimal SUSPICIOUS_AMOUNT = new BigDecimal("5000000.00"); // 5M XOF
    private static final int VELOCITY_CHECK_HOURS = 1;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 20;

    public FraudAnalysisResult analyzeTransaction(Long userId, BigDecimal amount, TransactionType type) {
        FraudAnalysisResult result = new FraudAnalysisResult();

        // Vérifier les montants suspects
        if (isSuspiciousAmount(amount)) {
            result.addFlag("SUSPICIOUS_AMOUNT", "Montant inhabituellement élevé");
            result.increaseRiskScore(30);
        }

        // Vérifier la vélocité des transactions
        if (hasHighVelocity(userId)) {
            result.addFlag("HIGH_VELOCITY", "Trop de transactions en peu de temps");
            result.increaseRiskScore(40);
        }

        // Vérifier les patterns suspects
        if (hasSuspiciousPattern(userId, amount, type)) {
            result.addFlag("SUSPICIOUS_PATTERN", "Pattern de transaction suspect détecté");
            result.increaseRiskScore(50);
        }

        // Vérifier les heures inhabituelles
        if (isUnusualTime()) {
            result.addFlag("UNUSUAL_TIME", "Transaction à une heure inhabituelle");
            result.increaseRiskScore(20);
        }

        // Vérifier les changements de comportement
        if (hasUnusualBehavior(userId, amount)) {
            result.addFlag("UNUSUAL_BEHAVIOR", "Comportement inhabituel détecté");
            result.increaseRiskScore(30);
        }

        // Déterminer le niveau de risque
        result.determineRiskLevel();

        return result;
    }

    private boolean isSuspiciousAmount(BigDecimal amount) {
        return amount.compareTo(SUSPICIOUS_AMOUNT) > 0;
    }

    private boolean hasHighVelocity(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(VELOCITY_CHECK_HOURS);
        List<ReserveFund> recentTransactions = reserveFundRepository
                .findByUserIdAndDateRange(userId, oneHourAgo, LocalDateTime.now());

        return recentTransactions.size() >= MAX_TRANSACTIONS_PER_HOUR;
    }

    private boolean hasSuspiciousPattern(Long userId, BigDecimal amount, TransactionType type) {
        // Rechercher des patterns de transactions répétitives avec le même montant
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<ReserveFund> recentTransactions = reserveFundRepository
                .findByUserIdAndDateRange(userId, oneDayAgo, LocalDateTime.now());

        long sameAmountCount = recentTransactions.stream()
                .filter(t -> t.getAmount().compareTo(amount) == 0)
                .count();

        return sameAmountCount >= 5; // 5 transactions identiques en 24h
    }

    private boolean isUnusualTime() {
        int hour = LocalDateTime.now().getHour();
        // Transactions entre 23h et 5h sont considérées comme inhabituelles
        return hour >= 23 || hour < 5;
    }

    private boolean hasUnusualBehavior(Long userId, BigDecimal amount) {
        // Comparer avec le montant moyen des transactions de l'utilisateur
        BigDecimal avgAmount = getAverageTransactionAmount(userId);

        if (avgAmount.compareTo(BigDecimal.ZERO) == 0) {
            return false; // Pas assez d'historique
        }

        // Si le montant est 5 fois supérieur à la moyenne
        BigDecimal threshold = avgAmount.multiply(new BigDecimal("5"));
        return amount.compareTo(threshold) > 0;
    }

    private BigDecimal getAverageTransactionAmount(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<ReserveFund> historicalTransactions = reserveFundRepository
                .findByUserIdAndDateRange(userId, thirtyDaysAgo, LocalDateTime.now());

        if (historicalTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = historicalTransactions.stream()
                .map(ReserveFund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(new BigDecimal(historicalTransactions.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean shouldBlockTransaction(FraudAnalysisResult result) {
        return result.getRiskLevel() == RiskLevel.CRITICAL ||
                result.getRiskScore() >= 80;
    }

    public boolean requiresManualReview(FraudAnalysisResult result) {
        return result.getRiskLevel() == RiskLevel.HIGH ||
                (result.getRiskLevel() == RiskLevel.MEDIUM && result.getRiskScore() >= 60);
    }

    // Classes internes
    public static class FraudAnalysisResult {
        private int riskScore = 0;
        private RiskLevel riskLevel = RiskLevel.LOW;
        private StringBuilder flags = new StringBuilder();

        public void addFlag(String code, String description) {
            if (flags.length() > 0) {
                flags.append("; ");
            }
            flags.append(code).append(": ").append(description);
        }

        public void increaseRiskScore(int points) {
            this.riskScore += points;
        }

        public void determineRiskLevel() {
            if (riskScore >= 80) {
                riskLevel = RiskLevel.CRITICAL;
            } else if (riskScore >= 60) {
                riskLevel = RiskLevel.HIGH;
            } else if (riskScore >= 40) {
                riskLevel = RiskLevel.MEDIUM;
            } else {
                riskLevel = RiskLevel.LOW;
            }
        }

        // Getters
        public int getRiskScore() { return riskScore; }
        public RiskLevel getRiskLevel() { return riskLevel; }
        public String getFlags() { return flags.toString(); }
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}