package com.example.demo.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Component that runs at application startup to initialize file storage directories
 * and create necessary folders if they don't exist.
 */
@Component
public class StorageInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);
    
    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Initializing file storage directories...");
            storageService.init();
            logger.info("Storage directories successfully initialized");
        } catch (Exception e) {
            logger.error("Error initializing storage directories", e);
            throw e;
        }
    }
}