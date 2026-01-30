package com.lidcoin.reserveFound.application.service;

import com.lidcoin.reserveFound.application.dto.ReserveFundDTO;
import com.lidcoin.reserveFound.domain.enums.FundStatus;
import com.lidcoin.reserveFound.domain.enums.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final Map<Long, CopyOnWriteArrayList<NotificationListener>> listeners = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void registerListener(Long userId, NotificationListener listener) {
        listeners.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.info("Listener enregistré pour l'utilisateur {}", userId);
    }

    public void unregisterListener(Long userId, NotificationListener listener) {
        CopyOnWriteArrayList<NotificationListener> userListeners = listeners.get(userId);
        if (userListeners != null) {
            userListeners.remove(listener);
            logger.info("Listener supprimé pour l'utilisateur {}", userId);
        }
    }

    public void notifyTransactionCreated(ReserveFundDTO transaction) {
        String message = buildTransactionMessage(transaction, "créée");
        sendNotification(transaction.getUserId(), NotificationType.TRANSACTION_CREATED, message, transaction);
        logger.info("Notification envoyée: Transaction créée pour l'utilisateur {}", transaction.getUserId());
    }

    public void notifyTransactionCompleted(ReserveFundDTO transaction) {
        String message = buildTransactionMessage(transaction, "complétée");
        sendNotification(transaction.getUserId(), NotificationType.TRANSACTION_COMPLETED, message, transaction);
        logger.info("Notification envoyée: Transaction complétée pour l'utilisateur {}", transaction.getUserId());
    }

    public void notifyTransactionFailed(ReserveFundDTO transaction, String reason) {
        String message = String.format("Transaction %s échouée. Raison: %s",
                transaction.getReference(), reason);
        sendNotification(transaction.getUserId(), NotificationType.TRANSACTION_FAILED, message, transaction);
        logger.warn("Notification envoyée: Transaction échouée pour l'utilisateur {}", transaction.getUserId());
    }

    public void notifyBalanceUpdated(Long userId, String balance) {
        String message = String.format("Votre solde a été mis à jour: %s XOF", balance);
        sendNotification(userId, NotificationType.BALANCE_UPDATED, message, null);
        logger.info("Notification envoyée: Solde mis à jour pour l'utilisateur {}", userId);
    }

    public void notifyLowBalance(Long userId, String currentBalance, String threshold) {
        String message = String.format("Attention: Votre solde (%s XOF) est inférieur au seuil (%s XOF)",
                currentBalance, threshold);
        sendNotification(userId, NotificationType.LOW_BALANCE_ALERT, message, null);
        logger.warn("Notification envoyée: Solde faible pour l'utilisateur {}", userId);
    }

    public void notifySecurityAlert(Long userId, String alertMessage) {
        String message = "Alerte de sécurité: " + alertMessage;
        sendNotification(userId, NotificationType.SECURITY_ALERT, message, null);
        logger.error("Notification envoyée: Alerte de sécurité pour l'utilisateur {}", userId);
    }

    public void notifyLargeTransaction(Long userId, ReserveFundDTO transaction) {
        String message = String.format("Transaction importante détectée: %s XOF - %s",
                transaction.getAmount(), transaction.getDescription());
        sendNotification(userId, NotificationType.LARGE_TRANSACTION, message, transaction);
        logger.info("Notification envoyée: Grande transaction pour l'utilisateur {}", userId);
    }

    public void notifyStatusChange(ReserveFundDTO transaction, FundStatus oldStatus, FundStatus newStatus) {
        String message = String.format("Statut de la transaction %s changé: %s → %s",
                transaction.getReference(), oldStatus.getDisplayName(), newStatus.getDisplayName());
        sendNotification(transaction.getUserId(), NotificationType.STATUS_CHANGED, message, transaction);
        logger.info("Notification envoyée: Changement de statut pour l'utilisateur {}", transaction.getUserId());
    }

    public void notifyDailyLimitReached(Long userId, String limit) {
        String message = String.format("Limite quotidienne atteinte: %s XOF", limit);
        sendNotification(userId, NotificationType.LIMIT_REACHED, message, null);
        logger.warn("Notification envoyée: Limite quotidienne atteinte pour l'utilisateur {}", userId);
    }

    public void notifyTransferReceived(Long userId, String amount, String fromUser) {
        String message = String.format("Vous avez reçu un transfert de %s XOF de %s", amount, fromUser);
        sendNotification(userId, NotificationType.TRANSFER_RECEIVED, message, null);
        logger.info("Notification envoyée: Transfert reçu pour l'utilisateur {}", userId);
    }

    private void sendNotification(Long userId, NotificationType type, String message, ReserveFundDTO data) {
        Notification notification = new Notification(type, message, LocalDateTime.now(), data);

        CopyOnWriteArrayList<NotificationListener> userListeners = listeners.get(userId);
        if (userListeners != null && !userListeners.isEmpty()) {
            for (NotificationListener listener : userListeners) {
                try {
                    listener.onNotification(notification);
                } catch (Exception e) {
                    logger.error("Erreur lors de l'envoi de notification à l'utilisateur {}", userId, e);
                }
            }
        } else {
            // Pas de listener enregistré, logger la notification
            logger.info("Notification pour l'utilisateur {} (pas de listener): {}", userId, message);
        }

        // Envoyer également par email/SMS/push (à implémenter)
        sendEmailNotification(userId, notification);
    }

    private String buildTransactionMessage(ReserveFundDTO transaction, String action) {
        return String.format("Transaction %s %s: %s XOF - %s (%s)",
                transaction.getReference(),
                action,
                transaction.getAmount(),
                getTransactionTypeLabel(transaction.getTransactionType()),
                transaction.getCreatedDate().format(formatter));
    }

    private String getTransactionTypeLabel(TransactionType type) {
        switch (type) {
            case DEPOSIT: return "Dépôt";
            case WITHDRAWAL: return "Retrait";
            case TRANSFER: return "Transfert";
            case ALLOCATION: return "Allocation";
            case ADJUSTMENT: return "Ajustement";
            case INTEREST: return "Intérêts";
            case FEE: return "Frais";
            case REFUND: return "Remboursement";
            default: return type.getDisplayName();
        }
    }

    private void sendEmailNotification(Long userId, Notification notification) {
        // TODO: Implémenter l'envoi d'email
        logger.debug("Email notification simulé pour l'utilisateur {}: {}", userId, notification.getMessage());
    }

    // Classes internes
    public interface NotificationListener {
        void onNotification(Notification notification);
    }

    public static class Notification {
        private final NotificationType type;
        private final String message;
        private final LocalDateTime timestamp;
        private final ReserveFundDTO data;

        public Notification(NotificationType type, String message, LocalDateTime timestamp, ReserveFundDTO data) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
            this.data = data;
        }

        public NotificationType getType() { return type; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public ReserveFundDTO getData() { return data; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            map.put("message", message);
            map.put("timestamp", timestamp);
            if (data != null) {
                map.put("data", data);
            }
            return map;
        }
    }

    public enum NotificationType {
        TRANSACTION_CREATED,
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED,
        BALANCE_UPDATED,
        LOW_BALANCE_ALERT,
        SECURITY_ALERT,
        LARGE_TRANSACTION,
        STATUS_CHANGED,
        LIMIT_REACHED,
        TRANSFER_RECEIVED
    }
}