package com.lidcoin.reserveFound.infrastructure.repository;

import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import com.lidcoin.reserveFound.domain.model.ReserveFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReserveFundRepository extends JpaRepository<ReserveFund, Long> {

    Optional<ReserveFund> findByReference(String reference);

    List<ReserveFund> findByStatus(FundStatus status);

    List<ReserveFund> findByUserId(Long userId);

    List<ReserveFund> findByTransactionType(TransactionType transactionType);

    List<ReserveFund> findByUserIdAndStatus(Long userId, FundStatus status);

    @Query("SELECT SUM(rf.balance) FROM ReserveFund rf WHERE rf.status = :status")
    BigDecimal getTotalBalanceByStatus(@Param("status") FundStatus status);

    @Query("SELECT SUM(rf.balance) FROM ReserveFund rf WHERE rf.status = 'ACTIVE'")
    BigDecimal getTotalActiveBalance();

    @Query("SELECT rf FROM ReserveFund rf WHERE rf.createdDate BETWEEN :startDate AND :endDate")
    List<ReserveFund> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT rf FROM ReserveFund rf WHERE rf.userId = :userId AND rf.createdDate BETWEEN :startDate AND :endDate")
    List<ReserveFund> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT rf FROM ReserveFund rf WHERE rf.transactionType = :type AND rf.status = :status")
    List<ReserveFund> findByTransactionTypeAndStatus(@Param("type") TransactionType type,
                                                     @Param("status") FundStatus status);

    @Query("SELECT SUM(rf.amount) FROM ReserveFund rf WHERE rf.userId = :userId AND rf.transactionType = :type")
    BigDecimal getTotalAmountByUserIdAndType(@Param("userId") Long userId,
                                             @Param("type") TransactionType type);

    boolean existsByReference(String reference);

    @Query("SELECT rf FROM ReserveFund rf WHERE rf.balance > :minBalance ORDER BY rf.balance DESC")
    List<ReserveFund> findByBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    List<ReserveFund> findTop10ByOrderByCreatedDateDesc();

    @Query("SELECT rf FROM ReserveFund rf WHERE rf.status = :status AND rf.updatedDate < :date")
    List<ReserveFund> findStaleTransactions(@Param("status") FundStatus status,
                                            @Param("date") LocalDateTime date);
}