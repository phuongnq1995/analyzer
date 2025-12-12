package org.phuongnq.analyzer.utils;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.repository.entity.Role;
import org.phuongnq.analyzer.repository.entity.User;
import org.phuongnq.analyzer.repository.RoleRepository;
import org.phuongnq.analyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.user:admin}")
    private String adminUser;

    @Value("${admin.password:admin}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        if (userRepository.findByUsername(adminUser).isEmpty()) {
            User admin = User.builder()
                    .username(adminUser)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole, userRole))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .build();
            userRepository.save(admin);
        }
    }
}
