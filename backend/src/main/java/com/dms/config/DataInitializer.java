package com.dms.config;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!prod") // don't run in production profile
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminStaffId = "ADMIN001";
        String adminPassword = "admin123";

        userRepository.findByStaffId(adminStaffId).ifPresentOrElse(user -> {
            boolean matches = passwordEncoder.matches(adminPassword, user.getPassword());
            if (!matches) {
                user.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(user);
                logger.info("Existing admin password reset to default for staffId={}", adminStaffId);
            } else {
                logger.info("Admin user exists and password matches for staffId={}", adminStaffId);
            }
        }, () -> {
            User admin = User.builder()
                    .staffId(adminStaffId)
                    .username("管理员")
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .status("ACTIVE")
                    .email("admin@company.com")
                    .phone("13800000001")
                    .build();
            userRepository.save(admin);
            logger.info("Created default admin user with staffId={} and password=admin123", adminStaffId);
        });
    }
}

