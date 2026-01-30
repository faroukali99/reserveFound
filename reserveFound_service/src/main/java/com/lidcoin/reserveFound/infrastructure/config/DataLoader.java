package com.lidcoin.reserveFound.infrastructure.config;

import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import com.lidcoin.reserveFound.infrastructure.repository.ReserveFundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final ReserveFundRepository reserveFundRepository;

    @Autowired
    public DataLoader(ReserveFundRepository reserveFundRepository) {
        this.reserveFundRepository = reserveFundRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (reserveFundRepository.count() == 0) {
            createSampleData();
        }
    }

    private void createSampleData() {
        // Créer un dépôt initial pour l'utilisateur 1 (superadmin du user service)
        ReserveFund deposit1 = new ReserveFund();
        deposit1.setUserId(1L);
        deposit1.setAmount(new BigDecimal("1000000.00"));
        deposit1.setBalance(new BigDecimal("1000000.00"));
        deposit1.setCurrency("XOF");
        deposit1.setTransactionType(TransactionType.DEPOSIT);
        deposit1.setDescription("Dépôt initial - Fonds de réserve système");
        deposit1.setReference("RF-INIT001");
        deposit1.setStatus(FundStatus.COMPLETED);
        deposit1.setCreatedBy("system");
        deposit1.setCreatedDate(LocalDateTime.now());
        reserveFundRepository.save(deposit1);

        // Créer un dépôt pour l'utilisateur 2 (admin)
        ReserveFund deposit2 = new ReserveFund();
        deposit2.setUserId(2L);
        deposit2.setAmount(new BigDecimal("500000.00"));
        deposit2.setBalance(new BigDecimal("500000.00"));
        deposit2.setCurrency("XOF");
        deposit2.setTransactionType(TransactionType.DEPOSIT);
        deposit2.setDescription("Dépôt initial - Admin");
        deposit2.setReference("RF-INIT002");
        deposit2.setStatus(FundStatus.COMPLETED);
        deposit2.setCreatedBy("system");
        deposit2.setCreatedDate(LocalDateTime.now());
        reserveFundRepository.save(deposit2);

        // Créer un dépôt pour l'utilisateur 3 (testuser)
        ReserveFund deposit3 = new ReserveFund();
        deposit3.setUserId(3L);
        deposit3.setAmount(new BigDecimal("250000.00"));
        deposit3.setBalance(new BigDecimal("250000.00"));
        deposit3.setCurrency("XOF");
        deposit3.setTransactionType(TransactionType.DEPOSIT);
        deposit3.setDescription("Dépôt initial - Utilisateur test");
        deposit3.setReference("RF-INIT003");
        deposit3.setStatus(FundStatus.COMPLETED);
        deposit3.setCreatedBy("system");
        deposit3.setCreatedDate(LocalDateTime.now());
        reserveFundRepository.save(deposit3);

        // Créer quelques transactions en attente
        ReserveFund pending = new ReserveFund();
        pending.setUserId(4L);
        pending.setAmount(new BigDecimal("100000.00"));
        pending.setBalance(BigDecimal.ZERO);
        pending.setCurrency("XOF");
        pending.setTransactionType(TransactionType.DEPOSIT);
        pending.setDescription("Dépôt en attente de validation");
        pending.setReference("RF-PEND001");
        pending.setStatus(FundStatus.PENDING);
        pending.setCreatedBy("system");
        pending.setCreatedDate(LocalDateTime.now());
        reserveFundRepository.save(pending);

        System.out.println("=================================================");
        System.out.println("Données de test créées avec succès:");
        System.out.println("- Dépôt initial User 1: 1,000,000 XOF");
        System.out.println("- Dépôt initial User 2: 500,000 XOF");
        System.out.println("- Dépôt initial User 3: 250,000 XOF");
        System.out.println("- Transaction en attente: 100,000 XOF");
        System.out.println("=================================================");
    }
}