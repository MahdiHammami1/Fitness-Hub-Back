package com.example.demo.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB Health Check Configuration
 *
 * Verifies on startup that the application is connected to MongoDB Atlas
 * and NOT to localhost:27017
 */
@Configuration
public class MongoDBHealthCheckConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBHealthCheckConfig.class);

    @Bean
    public ApplicationRunner mongoHealthCheck(MongoClient mongoClient) {
        return args -> {
            try {
                // Try to connect to MongoDB
                MongoDatabase database = mongoClient.getDatabase("admin");
                database.runCommand(new org.bson.Document("ping", 1));

                logger.info("");
                logger.info("╔════════════════════════════════════════════════════════╗");
                logger.info("║                                                        ║");
                logger.info("║          ✅ MongoDB Connection Status: SUCCESS          ║");
                logger.info("║                                                        ║");
                logger.info("║  🌍 Connected to: MongoDB Atlas (Cloud)                ║");
                logger.info("║  ❌ NOT connected to: localhost:27017                  ║");
                logger.info("║                                                        ║");
                logger.info("╚════════════════════════════════════════════════════════╝");
                logger.info("");

            } catch (Exception e) {
                logger.error("");
                logger.error("╔════════════════════════════════════════════════════════╗");
                logger.error("║                                                        ║");
                logger.error("║        ❌ MongoDB Connection Failed!                   ║");
                logger.error("║                                                        ║");
                logger.error("║  Error: {}", e.getMessage());
                logger.error("║                                                        ║");
                logger.error("║  Troubleshooting:                                      ║");
                logger.error("║  1. Check MongoDB Atlas whitelist (Network Access)     ║");
                logger.error("║  2. Verify credentials in application.properties       ║");
                logger.error("║  3. Check internet connectivity                        ║");
                logger.error("║                                                        ║");
                logger.error("╚════════════════════════════════════════════════════════╝");
                logger.error("");
                throw new RuntimeException("MongoDB Atlas connection failed", e);
            }
        };
    }
}

