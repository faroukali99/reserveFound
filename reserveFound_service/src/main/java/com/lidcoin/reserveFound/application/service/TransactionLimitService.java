package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.excption.TransactionLimitExceededException;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import com.lidcoin.reserveFound.infrastructure.repository.ReserveFundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionLimitService {

    @Autowired
    private ReserveFundRepository reserveFundRepository;

    // Limites par défaut (peuvent être configurées par utilisateur/niveau KYC)
    private static final Map<String, TransactionLimit> DEFAULT_LIMITS = new HashMap<>();

    static {
        // Limites pour utilisateurs standard (KYC niveau 1)
        DEFAULT_LIMITS.put("STANDARD_DAILY", new TransactionLimit(
                new BigDecimal("1000000.00"),  // 1M XOF par jour
                10,  // 10 transactions par jour
                new BigDecimal("100000.00")  // 100K XOF par transaction
        ));

        // Limites pour utilisateurs vérifiés (KYC niveau 2)
        DEFAULT_LIMITS.put("VERIFIED_DAILY", new TransactionLimit(
                new BigDecimal("5000000.00"),  // 5M XOF par jour
                50,  // 50 transactions par jour
                new BigDecimal("500000.00")  // 500K XOF par transaction
        ));

        // Limites pour utilisateurs premium (KYC niveau 3)
        DEFAULT_LIMITS.put("PREMIUM_DAILY", new TransactionLimit(
                new BigDecimal("50000000.00"),  // 50M XOF par jour
                100,  // 100 transactions par jour
                new BigDecimal("5000000.00")  // 5M XOF par transaction
        ));

        // Limites hebdomadaires
        DEFAULT_LIMITS.put("STANDARD_WEEKLY", new TransactionLimit(
                new BigDecimal("5000000.00"),
                50,
                new BigDecimal("100000.00")
        ));

        DEFAULT_LIMITS.put("VERIFIED_WEEKLY", new TransactionLimit(
                new BigDecimal("25000000.00"),
                200,
                new BigDecimal("500000.00")
        ));

        DEFAULT_LIMITS.put("PREMIUM_WEEKLY", new TransactionLimit(
                new BigDecimal("250000000.00"),
                500,
                new BigDecimal("5000000.00")
        ));

        // Limites mensuelles
        DEFAULT_LIMITS.put("STANDARD_MONTHLY", new TransactionLimit(
                new BigDecimal("20000000.00"),
                200,
                new BigDecimal("100000.00")
        ));

        DEFAULT_LIMITS.put("VERIFIED_MONTHLY", new TransactionLimit(
                new BigDecimal("100000000.00"),
                800,
                new BigDecimal("500000.00")
        ));

        DEFAULT_LIMITS.put("PREMIUM_MONTHLY", new TransactionLimit(
                new BigDecimal("1000000000.00"),
                2000,
                new BigDecimal("5000000.00")
        ));
    }

    public void checkDailyLimit(Long userId, BigDecimal amount, int kycLevel) {
        String limitKey = getLimitKey(kycLevel, "DAILY");
        TransactionLimit limit = DEFAULT_LIMITS.get(limitKey);

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        checkLimit(userId, amount, limit, startOfDay, endOfDay, "quotidienne");
    }

    public void checkWeeklyLimit(Long userId, BigDecimal amount, int kycLevel) {
        String limitKey = getLimitKey(kycLevel, "WEEKLY");
        TransactionLimit limit = DEFAULT_LIMITS.get(limitKey);

        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        checkLimit(userId, amount, limit, startOfWeek, now, "hebdomadaire");
    }

    public void checkMonthlyLimit(Long userId, BigDecimal amount, int kycLevel) {
        String limitKey = getLimitKey(kycLevel, "MONTHLY");
        TransactionLimit limit = DEFAULT_LIMITS.get(limitKey);

        LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();

        checkLimit(userId, amount, limit, startOfMonth, now, "mensuelle");
    }

    private void checkLimit(Long userId, BigDecimal amount, TransactionLimit limit,
                            LocalDateTime start, LocalDateTime end, String periodLabel) {
        List<ReserveFund> transactions = reserveFundRepository.findByUserIdAndDateRange(userId, start, end);

        // Vérifier la limite de nombre de transactions
        if (transactions.size() >= limit.getMaxTransactionCount()) {
            throw new TransactionLimitExceededException(
                    String.format("Limite %s de transactions atteinte (%d transactions)",
                            periodLabel, limit.getMaxTransactionCount())
            );
        }

        // Vérifier la limite de montant par transaction
        if (amount.compareTo(limit.getMaxTransactionAmount()) > 0) {
            throw new TransactionLimitExceededException(
                    String.format("Montant maximum par transaction dépassé. Limite: %s XOF, Demandé: %s XOF",
                            limit.getMaxTransactionAmount(), amount)
            );
        }

        // Calculer le total des transactions de la période
        BigDecimal totalAmount = transactions.stream()
                .map(ReserveFund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Vérifier la limite de montant total
        if (totalAmount.add(amount).compareTo(limit.getMaxTotalAmount()) > 0) {
            throw new TransactionLimitExceededException(
                    String.format("Limite %s de montant total dépassée. Limite: %s XOF, Utilisé: %s XOF, Demandé: %s XOF",
                            periodLabel, limit.getMaxTotalAmount(), totalAmount, amount)
            );
        }
    }

    public Map<String, Object> getRemainingLimits(Long userId, int kycLevel) {
        Map<String, Object> limits = new HashMap<>();

        // Limites quotidiennes
        limits.put("daily", getRemainingLimit(userId, kycLevel, "DAILY",
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)));

        // Limites hebdomadaires
        limits.put("weekly", getRemainingLimit(userId, kycLevel, "WEEKLY",
                LocalDateTime.now().minusDays(7), LocalDateTime.now()));

        // Limites mensuelles
        limits.put("monthly", getRemainingLimit(userId, kycLevel, "MONTHLY",
                LocalDateTime.now().minusDays(30), LocalDateTime.now()));

        return limits;
    }

    private Map<String, Object> getRemainingLimit(Long userId, int kycLevel, String period,
                                                  LocalDateTime start, LocalDateTime end) {
        String limitKey = getLimitKey(kycLevel, period);
        TransactionLimit limit = DEFAULT_LIMITS.get(limitKey);

        List<ReserveFund> transactions = reserveFundRepository.findByUserIdAndDateRange(userId, start, end);

        BigDecimal usedAmount = transactions.stream()
                .map(ReserveFund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> remaining = new HashMap<>();
        remaining.put("maxTotalAmount", limit.getMaxTotalAmount());
        remaining.put("usedAmount", usedAmount);
        remaining.put("remainingAmount", limit.getMaxTotalAmount().subtract(usedAmount));
        remaining.put("maxTransactionCount", limit.getMaxTransactionCount());
        remaining.put("usedTransactionCount", transactions.size());
        remaining.put("remainingTransactionCount", limit.getMaxTransactionCount() - transactions.size());
        remaining.put("maxTransactionAmount", limit.getMaxTransactionAmount());

        return remaining;
    }

    private String getLimitKey(int kycLevel, String period) {
        String level;
        switch (kycLevel) {
            case 1: level = "STANDARD"; break;
            case 2: level = "VERIFIED"; break;
            case 3: level = "PREMIUM"; break;
            default: level = "STANDARD";
        }
        return level + "_" + period;
    }

    public TransactionLimit getLimitForUser(int kycLevel, String period) {
        String limitKey = getLimitKey(kycLevel, period);
        return DEFAULT_LIMITS.get(limitKey);
    }

    public boolean canProcessTransaction(Long userId, BigDecimal amount, int kycLevel) {
        try {
            checkDailyLimit(userId, amount, kycLevel);
            checkWeeklyLimit(userId, amount, kycLevel);
            checkMonthlyLimit(userId, amount, kycLevel);
            return true;
        } catch (TransactionLimitExceededException e) {
            return false;
        }
    }

    // Classe interne pour les limites
    public static class TransactionLimit {
        private final BigDecimal maxTotalAmount;
        private final int maxTransactionCount;
        private final BigDecimal maxTransactionAmount;

        public TransactionLimit(BigDecimal maxTotalAmount, int maxTransactionCount, BigDecimal maxTransactionAmount) {
            this.maxTotalAmount = maxTotalAmount;
            this.maxTransactionCount = maxTransactionCount;
            this.maxTransactionAmount = maxTransactionAmount;
        }

        public BigDecimal getMaxTotalAmount() { return maxTotalAmount; }
        public int getMaxTransactionCount() { return maxTransactionCount; }
        public BigDecimal getMaxTransactionAmount() { return maxTransactionAmount; }
    }
}