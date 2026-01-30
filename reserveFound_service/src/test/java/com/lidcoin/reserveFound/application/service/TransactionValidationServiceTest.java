package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.application.dto.ReserveFundDTO;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.excption.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidationServiceTest {

    private TransactionValidationService validationService;
    private ReserveFundDTO validDTO;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService();

        validDTO = new ReserveFundDTO();
        validDTO.setAmount(new BigDecimal("50000"));
        validDTO.setCurrency("XOF");
        validDTO.setTransactionType(TransactionType.DEPOSIT);
        validDTO.setDescription("Test transaction");
    }

    @Test
    void testValidateTransactionSuccess() {
        assertDoesNotThrow(() -> validationService.validateTransaction(validDTO));
    }

    @Test
    void testValidateAmountNull() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateAmount(null);
        });
    }

    @Test
    void testValidateAmountNegative() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateAmount(new BigDecimal("-100"));
        });
    }

    @Test
    void testValidateAmountTooSmall() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateAmount(new BigDecimal("50"));
        });
    }

    @Test
    void testValidateAmountTooLarge() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateAmount(new BigDecimal("20000000"));
        });
    }

    @Test
    void testValidateAmountTooManyDecimals() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateAmount(new BigDecimal("1000.999"));
        });
    }

    @Test
    void testValidateCurrencyNull() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateCurrency(null);
        });
    }

    @Test
    void testValidateCurrencyInvalid() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateCurrency("USD");
        });
    }

    @Test
    void testValidateCurrencyValid() {
        assertDoesNotThrow(() -> validationService.validateCurrency("XOF"));
    }

    @Test
    void testValidateDescriptionTooLong() {
        String longDescription = "A".repeat(501);
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateDescription(longDescription);
        });
    }

    @Test
    void testValidateTransferSameAccount() {
        assertThrows(InvalidTransactionException.class, () -> {
            validationService.validateTransfer(1L, 1L, new BigDecimal("1000"));
        });
    }

    @Test
    void testValidateTransferSuccess() {
        assertDoesNotThrow(() -> {
            validationService.validateTransfer(1L, 2L, new BigDecimal("1000"));
        });
    }

    @Test
    void testIsHighRiskTransaction() {
        assertTrue(validationService.isHighRiskTransaction(
                new BigDecimal("6000000"), TransactionType.DEPOSIT));

        assertTrue(validationService.isHighRiskTransaction(
                new BigDecimal("1000"), TransactionType.WITHDRAWAL));

        assertFalse(validationService.isHighRiskTransaction(
                new BigDecimal("1000"), TransactionType.DEPOSIT));
    }

    @Test
    void testRequiresAdditionalVerification() {
        assertTrue(validationService.requiresAdditionalVerification(
                new BigDecimal("1500000"), 1L));

        assertFalse(validationService.requiresAdditionalVerification(
                new BigDecimal("500000"), 1L));
    }

    @Test
    void testCalculateTransactionFee() {
        BigDecimal fee = validationService.calculateTransactionFee(
                new BigDecimal("100000"), TransactionType.WITHDRAWAL);

        assertTrue(fee.compareTo(BigDecimal.ZERO) > 0);

        BigDecimal noFee = validationService.calculateTransactionFee(
                new BigDecimal("100000"), TransactionType.DEPOSIT);

        assertEquals(BigDecimal.ZERO, noFee);
    }
}