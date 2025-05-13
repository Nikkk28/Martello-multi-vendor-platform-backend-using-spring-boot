package com.martello.ecommerce.service;

import com.martello.ecommerce.model.entity.Notification;
import com.martello.ecommerce.model.entity.User;
import com.martello.ecommerce.model.enums.Role;
import com.martello.ecommerce.repository.NotificationRepository;
import com.martello.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendNotification(User recipient, String title, String content) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .content(content)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyAdmins(String title, String content) {
        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .toList();
        
        for (User admin : admins) {
            sendNotification(admin, title, content);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }
}
