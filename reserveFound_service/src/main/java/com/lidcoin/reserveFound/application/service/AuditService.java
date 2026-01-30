package com.lidcoin.reserveFound.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lidcoin.reserveFound.domain.model.AuditLog;
import com.lidcoin.reserveFound.infrastructure.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logAction(String entityType, Long entityId, String action, Long userId, String username) {
        AuditLog auditLog = new AuditLog(entityType, entityId, action, userId, username);
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("SUCCESS");
        auditLogRepository.save(auditLog);
        logger.info("Audit log créé: {} {} par {}", action, entityType, username);
    }

    public void logCreate(String entityType, Long entityId, Object entity, Long userId, String username) {
        AuditLog auditLog = new AuditLog(entityType, entityId, "CREATE", userId, username);
        try {
            auditLog.setNewValue(objectMapper.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            logger.error("Erreur lors de la sérialisation de l'entité", e);
        }
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("SUCCESS");
        auditLog.setDescription(String.format("Création de %s avec ID %d", entityType, entityId));
        auditLogRepository.save(auditLog);
    }

    public void logUpdate(String entityType, Long entityId, Object oldEntity, Object newEntity,
                          Long userId, String username, String changedFields) {
        AuditLog auditLog = new AuditLog(entityType, entityId, "UPDATE", userId, username);
        try {
            auditLog.setOldValue(objectMapper.writeValueAsString(oldEntity));
            auditLog.setNewValue(objectMapper.writeValueAsString(newEntity));
            auditLog.setChangedFields(changedFields);
        } catch (JsonProcessingException e) {
            logger.error("Erreur lors de la sérialisation des entités", e);
        }
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("SUCCESS");
        auditLog.setDescription(String.format("Mise à jour de %s avec ID %d. Champs modifiés: %s",
                entityType, entityId, changedFields));
        auditLogRepository.save(auditLog);
    }

    public void logDelete(String entityType, Long entityId, Object entity, Long userId, String username) {
        AuditLog auditLog = new AuditLog(entityType, entityId, "DELETE", userId, username);
        try {
            auditLog.setOldValue(objectMapper.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            logger.error("Erreur lors de la sérialisation de l'entité", e);
        }
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("SUCCESS");
        auditLog.setDescription(String.format("Suppression de %s avec ID %d", entityType, entityId));
        auditLogRepository.save(auditLog);
    }

    public void logRead(String entityType, Long entityId, Long userId, String username) {
        AuditLog auditLog = new AuditLog(entityType, entityId, "READ", userId, username);
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("SUCCESS");
        auditLog.setDescription(String.format("Consultation de %s avec ID %d", entityType, entityId));
        auditLogRepository.save(auditLog);
    }

    public void logFailedAction(String entityType, Long entityId, String action, Long userId,
                                String username, String errorMessage) {
        AuditLog auditLog = new AuditLog(entityType, entityId, action, userId, username);
        enrichWithRequestDetails(auditLog);
        auditLog.setStatus("FAILED");
        auditLog.setErrorMessage(errorMessage);
        auditLog.setDescription(String.format("Échec de %s pour %s avec ID %d", action, entityType, entityId));
        auditLogRepository.save(auditLog);
        logger.warn("Audit log échec: {} {} par {} - {}", action, entityType, username, errorMessage);
    }

    public void logSecurityEvent(String description, Long userId, String username, String severity) {
        AuditLog auditLog = new AuditLog("SECURITY_EVENT", null, "SECURITY_ALERT", userId, username);
        enrichWithRequestDetails(auditLog);
        auditLog.setDescription(description);
        auditLog.setStatus(severity);
        auditLogRepository.save(auditLog);
        logger.warn("Événement de sécurité: {} par {}", description, username);
    }

    public List<AuditLog> getAuditTrail(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public List<AuditLog> getUserAuditTrail(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getUserAuditTrailByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<AuditLog> getRecentAudits(int limit) {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<AuditLog> getAuditsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAuditsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    private void enrichWithRequestDetails(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestId(UUID.randomUUID().toString());
                auditLog.setSessionId(request.getSession(false) != null ?
                        request.getSession().getId() : null);
            }
        } catch (Exception e) {
            logger.debug("Impossible d'enrichir avec les détails de la requête", e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}