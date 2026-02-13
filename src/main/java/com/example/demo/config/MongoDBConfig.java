package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

/**
 * MongoDB Configuration
 *
 * This configuration FORCES the application to use the MongoDB URI
 * specified in application.properties (spring.data.mongodb.uri) instead of
 * connecting to localhost:27017.
 *
 * It creates explicit MongoClient and MongoDatabaseFactory beans to override
 * Spring's auto-configuration completely.
 */
@Configuration
public class MongoDBConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @PostConstruct
    public void logMongoDBConnection() {
        if (mongoUri != null && !mongoUri.isEmpty()) {
            String connectionType = mongoUri.contains("mongodb+srv") ? "MongoDB Atlas (Cloud)" : "MongoDB Local";
            logger.info("========================================");
            logger.info("✅ MongoDB Configuration");
            logger.info("   Type: {}", connectionType);
            logger.info("   URI loaded from: application.properties");
            logger.info("========================================");
        } else {
            logger.error("❌ CRITICAL: MongoDB URI not configured!");
            logger.error("   Set spring.data.mongodb.uri in application.properties");
        }
    }

    /**
     * Create explicit MongoClient from URI
     * This forces Spring to use our URI instead of localhost:27017
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoClient mongoClient() {
        logger.info("🔧 Creating MongoClient with URI: {}", mongoUri.replaceAll(":[^@]*@", ":***@"));
        MongoClient client = MongoClients.create(mongoUri);
        logger.info("✅ MongoClient created successfully");
        return client;
    }

    /**
     * Create MongoDatabaseFactory with explicit client
     * This prevents Spring from creating a default localhost connection
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        logger.info("🔧 Creating MongoDatabaseFactory");
        String databaseName = extractDatabaseName(mongoUri);
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
        logger.info("✅ MongoDatabaseFactory created with database: {}", databaseName);
        return factory;
    }

    /**
     * Create MongoTemplate bean
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        logger.info("🔗 Creating MongoTemplate");
        MongoTemplate template = new MongoTemplate(mongoDatabaseFactory);
        logger.info("✅ MongoTemplate initialized successfully");
        return template;
    }

    /**
     * Extract database name from MongoDB URI
     */
    private String extractDatabaseName(String uri) {
        // mongodb+srv://user:pass@host/dbname?appName=...
        // or mongodb://user:pass@host/dbname?appName=...
        try {
            String[] parts = uri.split("/");
            if (parts.length >= 4) {
                String dbPart = parts[3];
                String dbName = dbPart.split("\\?")[0];
                if (dbName != null && !dbName.isEmpty()) {
                    logger.debug("Extracted database name: {}", dbName);
                    return dbName;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract database name from URI: {}", e.getMessage());
        }
        logger.warn("Using default database name 'admin'");
        return "admin";
    }
}

