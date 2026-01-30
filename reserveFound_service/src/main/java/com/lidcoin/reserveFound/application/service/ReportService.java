package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import com.lidcoin.reserveFound.infrastructure.repository.ReserveFundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReserveFundRepository reserveFundRepository;

    public Map<String, Object> generateDailyReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<ReserveFund> transactions = reserveFundRepository.findByDateRange(startOfDay, endOfDay);

        return buildReport(transactions, "Rapport Quotidien", date.toString());
    }

    public Map<String, Object> generateWeeklyReport(LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(23, 59, 59);

        List<ReserveFund> transactions = reserveFundRepository.findByDateRange(start, end);

        return buildReport(transactions, "Rapport Hebdomadaire",
                weekStart + " - " + weekStart.plusDays(6));
    }

    public Map<String, Object> generateMonthlyReport(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ReserveFund> transactions = reserveFundRepository.findByDateRange(start, end);

        return buildReport(transactions, "Rapport Mensuel",
                startDate.getMonth() + " " + year);
    }

    public Map<String, Object> generateUserReport(Long userId, LocalDateTime start, LocalDateTime end) {
        List<ReserveFund> transactions = reserveFundRepository.findByUserIdAndDateRange(userId, start, end);

        Map<String, Object> report = buildReport(transactions, "Rapport Utilisateur",
                "ID: " + userId);
        report.put("userId", userId);

        return report;
    }

    public Map<String, Object> generateTransactionTypeReport(TransactionType type,
                                                             LocalDateTime start, LocalDateTime end) {
        List<ReserveFund> allTransactions = reserveFundRepository.findByDateRange(start, end);
        List<ReserveFund> transactions = allTransactions.stream()
                .filter(t -> t.getTransactionType() == type)
                .collect(Collectors.toList());

        return buildReport(transactions, "Rapport par Type", type.getDisplayName());
    }

    public Map<String, Object> generateComprehensiveReport(LocalDateTime start, LocalDateTime end) {
        List<ReserveFund> transactions = reserveFundRepository.findByDateRange(start, end);

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Rapport Complet");
        report.put("period", start + " - " + end);
        report.put("generatedAt", LocalDateTime.now());

        // Statistiques générales
        report.put("summary", buildSummary(transactions));

        // Par type de transaction
        report.put("byTransactionType", buildByTransactionType(transactions));

        // Par statut
        report.put("byStatus", buildByStatus(transactions));

        // Top utilisateurs
        report.put("topUsers", buildTopUsers(transactions, 10));

        // Tendances
        report.put("trends", buildTrends(transactions));

        // Analyse horaire
        report.put("hourlyAnalysis", buildHourlyAnalysis(transactions));

        // Montants moyens
        report.put("averages", buildAverages(transactions));

        return report;
    }

    private Map<String, Object> buildReport(List<ReserveFund> transactions, String reportType, String period) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", reportType);
        report.put("period", period);
        report.put("generatedAt", LocalDateTime.now());
        report.put("summary", buildSummary(transactions));
        report.put("byTransactionType", buildByTransactionType(transactions));
        report.put("byStatus", buildByStatus(transactions));
        report.put("topTransactions", buildTopTransactions(transactions, 10));

        return report;
    }

    private Map<String, Object> buildSummary(List<ReserveFund> transactions) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalTransactions", transactions.size());

        BigDecimal totalAmount = transactions.stream()
                .map(ReserveFund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalAmount", totalAmount);

        BigDecimal totalBalance = transactions.stream()
                .map(ReserveFund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("totalBalance", totalBalance);

        long completedCount = transactions.stream()
                .filter(t -> t.getStatus() == FundStatus.COMPLETED)
                .count();
        summary.put("completedTransactions", completedCount);

        long pendingCount = transactions.stream()
                .filter(t -> t.getStatus() == FundStatus.PENDING)
                .count();
        summary.put("pendingTransactions", pendingCount);

        long failedCount = transactions.stream()
                .filter(t -> t.getStatus() == FundStatus.FAILED)
                .count();
        summary.put("failedTransactions", failedCount);

        // Taux de succès
        if (transactions.size() > 0) {
            double successRate = (double) completedCount / transactions.size() * 100;
            summary.put("successRate", BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP));
        } else {
            summary.put("successRate", BigDecimal.ZERO);
        }

        return summary;
    }

    private Map<String, Object> buildByTransactionType(List<ReserveFund> transactions) {
        Map<String, Object> byType = new HashMap<>();

        for (TransactionType type : TransactionType.values()) {
            List<ReserveFund> typeTransactions = transactions.stream()
                    .filter(t -> t.getTransactionType() == type)
                    .collect(Collectors.toList());

            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("count", typeTransactions.size());

            BigDecimal typeTotal = typeTransactions.stream()
                    .map(ReserveFund::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            typeStats.put("totalAmount", typeTotal);

            if (!typeTransactions.isEmpty()) {
                BigDecimal avg = typeTotal.divide(new BigDecimal(typeTransactions.size()), 2, RoundingMode.HALF_UP);
                typeStats.put("averageAmount", avg);
            }

            byType.put(type.name(), typeStats);
        }

        return byType;
    }

    private Map<String, Object> buildByStatus(List<ReserveFund> transactions) {
        Map<String, Object> byStatus = new HashMap<>();

        for (FundStatus status : FundStatus.values()) {
            long count = transactions.stream()
                    .filter(t -> t.getStatus() == status)
                    .count();

            byStatus.put(status.name(), count);
        }

        return byStatus;
    }

    private List<Map<String, Object>> buildTopTransactions(List<ReserveFund> transactions, int limit) {
        return transactions.stream()
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .limit(limit)
                .map(this::transactionToMap)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildTopUsers(List<ReserveFund> transactions, int limit) {
        Map<Long, BigDecimal> userTotals = new HashMap<>();

        for (ReserveFund transaction : transactions) {
            Long userId = transaction.getUserId();
            if (userId != null) {
                userTotals.merge(userId, transaction.getAmount(), BigDecimal::add);
            }
        }

        return userTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> userStat = new HashMap<>();
                    userStat.put("userId", entry.getKey());
                    userStat.put("totalAmount", entry.getValue());
                    return userStat;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildTrends(List<ReserveFund> transactions) {
        Map<String, Object> trends = new HashMap<>();

        // Grouper par jour
        Map<LocalDate, List<ReserveFund>> byDay = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCreatedDate().toLocalDate()));

        List<Map<String, Object>> dailyTrends = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", entry.getKey());
                    dayData.put("count", entry.getValue().size());

                    BigDecimal dayTotal = entry.getValue().stream()
                            .map(ReserveFund::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    dayData.put("totalAmount", dayTotal);

                    return dayData;
                })
                .collect(Collectors.toList());

        trends.put("daily", dailyTrends);

        return trends;
    }

    private Map<String, Object> buildHourlyAnalysis(List<ReserveFund> transactions) {
        Map<Integer, Long> hourlyCount = new HashMap<>();

        for (ReserveFund transaction : transactions) {
            int hour = transaction.getCreatedDate().getHour();
            hourlyCount.merge(hour, 1L, Long::sum);
        }

        Map<String, Object> hourlyAnalysis = new HashMap<>();
        hourlyAnalysis.put("distribution", hourlyCount);

        // Heure la plus active
        if (!hourlyCount.isEmpty()) {
            int peakHour = hourlyCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
            hourlyAnalysis.put("peakHour", peakHour);
        }

        return hourlyAnalysis;
    }

    private Map<String, Object> buildAverages(List<ReserveFund> transactions) {
        Map<String, Object> averages = new HashMap<>();

        if (transactions.isEmpty()) {
            return averages;
        }

        BigDecimal totalAmount = transactions.stream()
                .map(ReserveFund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgTransaction = totalAmount.divide(
                new BigDecimal(transactions.size()), 2, RoundingMode.HALF_UP);
        averages.put("averageTransaction", avgTransaction);

        // Médiane
        List<BigDecimal> amounts = transactions.stream()
                .map(ReserveFund::getAmount)
                .sorted()
                .collect(Collectors.toList());

        BigDecimal median;
        int size = amounts.size();
        if (size % 2 == 0) {
            median = amounts.get(size / 2 - 1).add(amounts.get(size / 2))
                    .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        } else {
            median = amounts.get(size / 2);
        }
        averages.put("median", median);

        return averages;
    }

    private Map<String, Object> transactionToMap(ReserveFund transaction) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", transaction.getId());
        map.put("reference", transaction.getReference());
        map.put("amount", transaction.getAmount());
        map.put("type", transaction.getTransactionType());
        map.put("status", transaction.getStatus());
        map.put("userId", transaction.getUserId());
        map.put("createdDate", transaction.getCreatedDate());
        return map;
    }

    public byte[] exportReportToCsv(Map<String, Object> report) {
        // TODO: Implémenter l'export CSV
        return new byte[0];
    }

    public byte[] exportReportToPdf(Map<String, Object> report) {
        // TODO: Implémenter l'export PDF
        return new byte[0];
    }
}