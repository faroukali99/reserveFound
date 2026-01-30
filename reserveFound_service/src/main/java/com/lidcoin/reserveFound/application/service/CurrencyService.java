package com.lidcoin.reserveFound.application.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CurrencyService {

    // Taux de change par rapport au XOF (Franc CFA)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        EXCHANGE_RATES.put("XOF", BigDecimal.ONE); // Base
        EXCHANGE_RATES.put("EUR", new BigDecimal("655.957")); // 1 EUR = 655.957 XOF
        EXCHANGE_RATES.put("USD", new BigDecimal("600.00")); // 1 USD ≈ 600 XOF
        EXCHANGE_RATES.put("GBP", new BigDecimal("750.00")); // 1 GBP ≈ 750 XOF
        EXCHANGE_RATES.put("CHF", new BigDecimal("680.00")); // 1 CHF ≈ 680 XOF
        EXCHANGE_RATES.put("CAD", new BigDecimal("450.00")); // 1 CAD ≈ 450 XOF
        EXCHANGE_RATES.put("NGN", new BigDecimal("1.50")); // 1 NGN ≈ 1.5 XOF
        EXCHANGE_RATES.put("GHS", new BigDecimal("80.00")); // 1 GHS ≈ 80 XOF
    }

    private static final Set<String> SUPPORTED_CURRENCIES = EXCHANGE_RATES.keySet();

    public boolean isCurrencySupported(String currency) {
        return SUPPORTED_CURRENCIES.contains(currency);
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (!isCurrencySupported(fromCurrency)) {
            throw new IllegalArgumentException("Devise source non supportée: " + fromCurrency);
        }

        if (!isCurrencySupported(toCurrency)) {
            throw new IllegalArgumentException("Devise cible non supportée: " + toCurrency);
        }

        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        // Convertir d'abord vers XOF puis vers la devise cible
        BigDecimal amountInXof;
        if (fromCurrency.equals("XOF")) {
            amountInXof = amount;
        } else {
            amountInXof = amount.multiply(EXCHANGE_RATES.get(fromCurrency));
        }

        // Convertir de XOF vers la devise cible
        if (toCurrency.equals("XOF")) {
            return amountInXof.setScale(2, RoundingMode.HALF_UP);
        } else {
            return amountInXof.divide(EXCHANGE_RATES.get(toCurrency), 2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (!isCurrencySupported(fromCurrency) || !isCurrencySupported(toCurrency)) {
            throw new IllegalArgumentException("Devise non supportée");
        }

        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromRate = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal toRate = EXCHANGE_RATES.get(toCurrency);

        return fromRate.divide(toRate, 6, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> getAllExchangeRates(String baseCurrency) {
        if (!isCurrencySupported(baseCurrency)) {
            throw new IllegalArgumentException("Devise non supportée: " + baseCurrency);
        }

        Map<String, BigDecimal> rates = new HashMap<>();

        for (String currency : SUPPORTED_CURRENCIES) {
            if (!currency.equals(baseCurrency)) {
                rates.put(currency, getExchangeRate(baseCurrency, currency));
            }
        }

        return rates;
    }

    public Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    public Map<String, Object> getCurrencyInfo(String currency) {
        if (!isCurrencySupported(currency)) {
            throw new IllegalArgumentException("Devise non supportée: " + currency);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("code", currency);
        info.put("name", getCurrencyName(currency));
        info.put("symbol", getCurrencySymbol(currency));
        info.put("rateToXof", EXCHANGE_RATES.get(currency));

        return info;
    }

    private String getCurrencyName(String currency) {
        switch (currency) {
            case "XOF": return "Franc CFA (BCEAO)";
            case "EUR": return "Euro";
            case "USD": return "Dollar américain";
            case "GBP": return "Livre sterling";
            case "CHF": return "Franc suisse";
            case "CAD": return "Dollar canadien";
            case "NGN": return "Naira nigérian";
            case "GHS": return "Cedi ghanéen";
            default: return currency;
        }
    }

    private String getCurrencySymbol(String currency) {
        switch (currency) {
            case "XOF": return "CFA";
            case "EUR": return "€";
            case "USD": return "$";
            case "GBP": return "£";
            case "CHF": return "CHF";
            case "CAD": return "C$";
            case "NGN": return "₦";
            case "GHS": return "₵";
            default: return currency;
        }
    }

    public BigDecimal calculateConversionFee(BigDecimal amount, String fromCurrency, String toCurrency) {
        // Frais de conversion: 0.5%
        BigDecimal feeRate = new BigDecimal("0.005");

        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ZERO;
        }

        BigDecimal convertedAmount = convert(amount, fromCurrency, toCurrency);
        return convertedAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
    }

    // Mise à jour des taux (à implémenter avec une API externe)
    public void updateExchangeRates() {
        // TODO: Implémenter l'intégration avec une API de taux de change
        // Exemples: ECB API, Open Exchange Rates, etc.
    }
}