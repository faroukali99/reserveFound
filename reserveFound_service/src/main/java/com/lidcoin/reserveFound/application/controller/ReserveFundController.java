package com.lidcoin.reserveFound.application.controller;

import com.lidcoin.reserveFound.application.dto.ReserveFundDTO;
import com.lidcoin.reserveFound.application.service.ReserveFundService;
import com.lidcoin.reserveFound.domain.enums.FundStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reserve-funds")
@CrossOrigin(origins = "*")
public class ReserveFundController {

    private final ReserveFundService reserveFundService;

    @Autowired
    public ReserveFundController(ReserveFundService reserveFundService) {
        this.reserveFundService = reserveFundService;
    }

    @PostMapping
    public ResponseEntity<?> createReserveFund(@Valid @RequestBody ReserveFundDTO dto) {
        try {
            ReserveFundDTO created = reserveFundService.createReserveFund(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fonds de réserve créé avec succès");
            response.put("data", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        try {
            ReserveFundDTO deposit = reserveFundService.deposit(userId, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dépôt effectué avec succès");
            response.put("data", deposit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        try {
            ReserveFundDTO withdrawal = reserveFundService.withdraw(userId, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Retrait effectué avec succès");
            response.put("data", withdrawal);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        try {
            ReserveFundDTO transfer = reserveFundService.transfer(fromUserId, toUserId, amount, description);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transfert effectué avec succès");
            response.put("data", transfer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            ReserveFundDTO fund = reserveFundService.getById(id);
            return ResponseEntity.ok(fund);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getByReference(@PathVariable String reference) {
        try {
            ReserveFundDTO fund = reserveFundService.getByReference(reference);
            return ResponseEntity.ok(fund);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReserveFundDTO>> getAllByUserId(@PathVariable Long userId) {
        List<ReserveFundDTO> funds = reserveFundService.getAllByUserId(userId);
        return ResponseEntity.ok(funds);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReserveFundDTO>> getAllByStatus(@PathVariable FundStatus status) {
        List<ReserveFundDTO> funds = reserveFundService.getAllByStatus(status);
        return ResponseEntity.ok(funds);
    }

    @GetMapping
    public ResponseEntity<List<ReserveFundDTO>> getAll() {
        List<ReserveFundDTO> funds = reserveFundService.getAll();
        return ResponseEntity.ok(funds);
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<?> getTotalBalance(@PathVariable Long userId) {
        try {
            BigDecimal balance = reserveFundService.getTotalBalanceByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("balance", balance);
            response.put("currency", "XOF");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/balance/total")
    public ResponseEntity<?> getTotalActiveBalance() {
        try {
            BigDecimal totalBalance = reserveFundService.getTotalActiveBalance();
            Map<String, Object> response = new HashMap<>();
            response.put("totalBalance", totalBalance);
            response.put("currency", "XOF");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam FundStatus status) {
        try {
            ReserveFundDTO updated = reserveFundService.updateStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Statut mis à jour avec succès");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFund(@PathVariable Long id) {
        try {
            reserveFundService.deleteFund(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fonds annulé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<ReserveFundDTO>> getTransactionHistory(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ReserveFundDTO> history = reserveFundService.getTransactionHistory(userId, startDate, endDate);
        return ResponseEntity.ok(history);
    }
}