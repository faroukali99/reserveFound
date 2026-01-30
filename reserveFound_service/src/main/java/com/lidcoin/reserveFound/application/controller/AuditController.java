package com.lidcoin.reserveFound.application.controller;

import com.lidcoin.reserveFound.application.service.AuditService;
import com.lidcoin.reserveFound.domain.model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AuditLog>> getEntityAuditTrail(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLog> auditTrail = auditService.getAuditTrail(entityType, entityId);
        return ResponseEntity.ok(auditTrail);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditTrail(@PathVariable Long userId) {
        List<AuditLog> auditTrail = auditService.getUserAuditTrail(userId);
        return ResponseEntity.ok(auditTrail);
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<AuditLog>> getUserAuditTrailByRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> auditTrail = auditService.getUserAuditTrailByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(auditTrail);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AuditLog>> getRecentAudits() {
        List<AuditLog> auditLogs = auditService.getRecentAudits(100);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditsByAction(@PathVariable String action) {
        List<AuditLog> auditLogs = auditService.getAuditsByAction(action);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditService.getAuditsByDateRange(startDate, endDate);
        return ResponseEntity.ok(auditLogs);
    }
}