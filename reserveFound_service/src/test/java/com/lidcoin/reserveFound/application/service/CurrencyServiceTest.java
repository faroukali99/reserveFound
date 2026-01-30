package com.lidcoin.reserveFound.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyServiceTest {

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService();
    }

    @Test
    void testIsCurrencySupportedXOF() {
        assertTrue(currencyService.isCurrencySupported("XOF"));
    }

    @Test
    void testIsCurrencySupportedEUR() {
        assertTrue(currencyService.isCurrencySupported("EUR"));
    }

    @Test
    void testIsCurrencySupportedInvalid() {
        assertFalse(currencyService.isCurrencySupported("INVALID"));
    }

    @Test
    void testConvertSameCurrency() {
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal result = currencyService.convert(amount, "XOF", "XOF");

        assertEquals(amount, result);
    }

    @Test
    void testConvertXOFtoEUR() {
        BigDecimal amount = new BigDecimal("655.957");
        BigDecimal result = currencyService.convert(amount, "XOF", "EUR");

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testConvertEURtoXOF() {
        BigDecimal amount = new BigDecimal("1");
        BigDecimal result = currencyService.convert(amount, "EUR", "XOF");

        assertNotNull(result);
        assertEquals(new BigDecimal("655.96"), result);
    }

    @Test
    void testConvertUnsupportedCurrency() {
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convert(new BigDecimal("1000"), "XOF", "INVALID");
        });
    }

    @Test
    void testGetExchangeRateSameCurrency() {
        BigDecimal rate = currencyService.getExchangeRate("XOF", "XOF");
        assertEquals(BigDecimal.ONE, rate);
    }

    @Test
    void testGetExchangeRate() {
        BigDecimal rate = currencyService.getExchangeRate("EUR", "XOF");

        assertNotNull(rate);
        assertTrue(rate.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetAllExchangeRates() {
        Map<String, BigDecimal> rates = currencyService.getAllExchangeRates("XOF");

        assertNotNull(rates);
        assertFalse(rates.isEmpty());
        assertTrue(rates.containsKey("EUR"));
        assertTrue(rates.containsKey("USD"));
    }

    @Test
    void testGetSupportedCurrencies() {
        Set<String> currencies = currencyService.getSupportedCurrencies();

        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());
        assertTrue(currencies.contains("XOF"));
        assertTrue(currencies.contains("EUR"));
        assertTrue(currencies.contains("USD"));
    }

    @Test
    void testGetCurrencyInfo() {
        Map<String, Object> info = currencyService.getCurrencyInfo("XOF");

        assertNotNull(info);
        assertEquals("XOF", info.get("code"));
        assertTrue(info.containsKey("name"));
        assertTrue(info.containsKey("symbol"));
        assertTrue(info.containsKey("rateToXof"));
    }

    @Test
    void testGetCurrencyInfoUnsupported() {
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.getCurrencyInfo("INVALID");
        });
    }

    @Test
    void testCalculateConversionFeeSameCurrency() {
        BigDecimal fee = currencyService.calculateConversionFee(
                new BigDecimal("1000"), "XOF", "XOF");

        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void testCalculateConversionFeeDifferentCurrency() {
        BigDecimal fee = currencyService.calculateConversionFee(
                new BigDecimal("100000"), "XOF", "EUR");

        assertNotNull(fee);
        assertTrue(fee.compareTo(BigDecimal.ZERO) > 0);
    }
}