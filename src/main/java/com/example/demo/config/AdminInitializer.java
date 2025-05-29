package com.example.demo.config;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.repositories.UserRepository;
import com.example.demo.upload.StorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes admin user on application startup if it doesn't exist
 */
@Component
public class AdminInitializer implements CommandLineRunner {    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${admin.email:admin@admin.com}")
    private String adminEmail;
    
    @Value("${admin.username:admin}")
    private String adminUsername;
    
    @Value("${admin.name:admin}")
    private String adminName;
    
    @Value("${admin.password:admin}")
    private String adminPassword;

    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setUsername(adminUsername);
            admin.setName(adminName);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setActive(true);

            String randomImageUrl = storageService.getRandomUserImage();
            admin.setImageUrl(randomImageUrl);
            userRepository.save(admin);

            logger.info("✓ Admin user created successfully with email: {}", adminEmail);
        } else {
            logger.info("✓ Admin user already exists");
        }
    }
}
