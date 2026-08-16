package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<@NonNull User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<@NonNull User> getUserByKeycloakUserId(@NonNull String keycloakUserId) {
        return userRepository.findByKeycloakId(keycloakUserId);
    }

    @Override
    public @NonNull User createUser(@NonNull User user) {
        return userRepository.findByKeycloakId(user.getKeycloakId())
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    existing.setFirstName(user.getFirstName());
                    existing.setLastName(user.getLastName());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(user));
    }
}
