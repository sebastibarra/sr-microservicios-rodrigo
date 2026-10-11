package com.reservas.ms_notificaciones.service;

import com.reservas.commons.entity.User;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.ms_notificaciones.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService extends BaseService<User> {

    public UserService(UserRepository repository) {
        super(repository, "User");
    }

    @Transactional(readOnly = true)
    public User getActiveUser(UUID id) {
        User user = findOrThrow(id);
        if (!user.isActive()) {
            log.warn("User {} is inactive, notification rejected", id);
            throw new InvalidOperationException("The user is inactive");
        }
        return user;
    }
}
