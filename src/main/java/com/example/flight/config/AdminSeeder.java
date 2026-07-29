package com.example.flight.config;

import com.example.flight.entity.User;
import com.example.flight.repository.UserRepository;
import com.example.flight.security.Roles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Creates the initial administrator account from configuration, because
 * /auth/register can only ever create ROLE_USER accounts. Does nothing unless
 * both app.admin.username and app.admin.password are set.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminUsername) || !StringUtils.hasText(adminPassword)) {
            log.info("No app.admin.username/password configured — skipping admin seeding");
            return;
        }

        if (userRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user '{}' already exists — skipping admin seeding", adminUsername);
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Roles.ADMIN);
        userRepository.save(admin);

        log.info("Seeded admin user '{}'", adminUsername);
    }
}
