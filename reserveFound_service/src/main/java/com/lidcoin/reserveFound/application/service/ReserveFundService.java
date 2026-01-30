package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.application.dto.ReserveFundDTO;
import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.excption.InsufficientFundsException;
import com.lidcoin.reserveFound.domain.excption.ReserveFundNotFoundException;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import com.lidcoin.reserveFound.infrastructure.repository.ReserveFundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReserveFundService {

    private final ReserveFundRepository reserveFundRepository;

    @Autowired
    public ReserveFundService(ReserveFundRepository reserveFundRepository) {
        this.reserveFundRepository = reserveFundRepository;
    }

    public ReserveFundDTO createReserveFund(ReserveFundDTO dto) {
        ReserveFund reserveFund = toEntity(dto);

        // Générer une référence unique
        reserveFund.setReference(generateReference());

        // Définir le statut initial
        if (reserveFund.getStatus() == null) {
            reserveFund.setStatus(FundStatus.PENDING);
        }

        // Initialiser le solde si c'est un dépôt
        if (reserveFund.getTransactionType().isCredit()) {
            reserveFund.setBalance(reserveFund.getAmount());
        }

        ReserveFund saved = reserveFundRepository.save(reserveFund);
        return toDTO(saved);
    }

    public ReserveFundDTO deposit(Long userId, BigDecimal amount, String description) {
        ReserveFund reserveFund = new ReserveFund();
        reserveFund.setUserId(userId);
        reserveFund.setAmount(amount);
        reserveFund.setBalance(amount);
        reserveFund.setTransactionType(TransactionType.DEPOSIT);
        reserveFund.setDescription(description);
        reserveFund.setReference(generateReference());
        reserveFund.setStatus(FundStatus.COMPLETED);

        ReserveFund saved = reserveFundRepository.save(reserveFund);
        return toDTO(saved);
    }

    public ReserveFundDTO withdraw(Long userId, BigDecimal amount, String description) {
        // Vérifier le solde disponible
        BigDecimal totalBalance = getTotalBalanceByUserId(userId);

        if (totalBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Solde insuffisant pour effectuer ce retrait");
        }

        ReserveFund reserveFund = new ReserveFund();
        reserveFund.setUserId(userId);
        reserveFund.setAmount(amount);
        reserveFund.setBalance(amount.negate());
        reserveFund.setTransactionType(TransactionType.WITHDRAWAL);
        reserveFund.setDescription(description);
        reserveFund.setReference(generateReference());
        reserveFund.setStatus(FundStatus.COMPLETED);

        ReserveFund saved = reserveFundRepository.save(reserveFund);
        return toDTO(saved);
    }

    public ReserveFundDTO transfer(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        // Vérifier le solde de l'émetteur
        BigDecimal senderBalance = getTotalBalanceByUserId(fromUserId);

        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Solde insuffisant pour effectuer ce transfert");
        }

        // Créer la transaction de transfert
        ReserveFund transfer = new ReserveFund();
        transfer.setUserId(fromUserId);
        transfer.setAmount(amount);
        transfer.setTransactionType(TransactionType.TRANSFER);
        transfer.setDescription(description);
        transfer.setReference(generateReference());
        transfer.setSourceAccount(fromUserId.toString());
        transfer.setDestinationAccount(toUserId.toString());
        transfer.setStatus(FundStatus.COMPLETED);

        ReserveFund saved = reserveFundRepository.save(transfer);

        // Créer l'entrée pour le bénéficiaire
        ReserveFund receiverFund = new ReserveFund();
        receiverFund.setUserId(toUserId);
        receiverFund.setAmount(amount);
        receiverFund.setBalance(amount);
        receiverFund.setTransactionType(TransactionType.DEPOSIT);
        receiverFund.setDescription("Transfert reçu: " + description);
        receiverFund.setReference(generateReference());
        receiverFund.setStatus(FundStatus.COMPLETED);
        reserveFundRepository.save(receiverFund);

        return toDTO(saved);
    }

    public ReserveFundDTO getById(Long id) {
        ReserveFund reserveFund = reserveFundRepository.findById(id)
                .orElseThrow(() -> new ReserveFundNotFoundException("Fonds de réserve non trouvé avec l'ID: " + id));
        return toDTO(reserveFund);
    }

    public ReserveFundDTO getByReference(String reference) {
        ReserveFund reserveFund = reserveFundRepository.findByReference(reference)
                .orElseThrow(() -> new ReserveFundNotFoundException("Fonds de réserve non trouvé avec la référence: " + reference));
        return toDTO(reserveFund);
    }

    public List<ReserveFundDTO> getAllByUserId(Long userId) {
        return reserveFundRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReserveFundDTO> getAllByStatus(FundStatus status) {
        return reserveFundRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReserveFundDTO> getAll() {
        return reserveFundRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalBalanceByUserId(Long userId) {
        List<ReserveFund> funds = reserveFundRepository.findByUserId(userId);
        return funds.stream()
                .map(ReserveFund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalActiveBalance() {
        BigDecimal total = reserveFundRepository.getTotalActiveBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public ReserveFundDTO updateStatus(Long id, FundStatus newStatus) {
        ReserveFund reserveFund = reserveFundRepository.findById(id)
                .orElseThrow(() -> new ReserveFundNotFoundException("Fonds de réserve non trouvé avec l'ID: " + id));

        reserveFund.setStatus(newStatus);
        reserveFund.setUpdatedDate(LocalDateTime.now());

        ReserveFund updated = reserveFundRepository.save(reserveFund);
        return toDTO(updated);
    }

    public void deleteFund(Long id) {
        ReserveFund reserveFund = reserveFundRepository.findById(id)
                .orElseThrow(() -> new ReserveFundNotFoundException("Fonds de réserve non trouvé avec l'ID: " + id));

        reserveFund.setStatus(FundStatus.CANCELLED);
        reserveFundRepository.save(reserveFund);
    }

    public List<ReserveFundDTO> getTransactionHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return reserveFundRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires
    private String generateReference() {
        return "RF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ReserveFundDTO toDTO(ReserveFund entity) {
        if (entity == null) {
            return null;
        }

        ReserveFundDTO dto = new ReserveFundDTO();
        dto.setId(entity.getId());
        dto.setAmount(entity.getAmount());
        dto.setBalance(entity.getBalance());
        dto.setCurrency(entity.getCurrency());
        dto.setStatus(entity.getStatus());
        dto.setTransactionType(entity.getTransactionType());
        dto.setDescription(entity.getDescription());
        dto.setReference(entity.getReference());
        dto.setUserId(entity.getUserId());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setSourceAccount(entity.getSourceAccount());
        dto.setDestinationAccount(entity.getDestinationAccount());
        dto.setTransactionHash(entity.getTransactionHash());
        dto.setBlockNumber(entity.getBlockNumber());

        return dto;
    }

    private ReserveFund toEntity(ReserveFundDTO dto) {
        if (dto == null) {
            return null;
        }

        ReserveFund entity = new ReserveFund();
        entity.setId(dto.getId());
        entity.setAmount(dto.getAmount());
        entity.setBalance(dto.getBalance());
        entity.setCurrency(dto.getCurrency());
        entity.setStatus(dto.getStatus());
        entity.setTransactionType(dto.getTransactionType());
        entity.setDescription(dto.getDescription());
        entity.setReference(dto.getReference());
        entity.setUserId(dto.getUserId());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setUpdatedDate(dto.getUpdatedDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setSourceAccount(dto.getSourceAccount());
        entity.setDestinationAccount(dto.getDestinationAccount());
        entity.setTransactionHash(dto.getTransactionHash());
        entity.setBlockNumber(dto.getBlockNumber());

        return entity;
    }
}