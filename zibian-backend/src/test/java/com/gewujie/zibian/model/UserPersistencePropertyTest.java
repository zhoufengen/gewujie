package com.gewujie.zibian.model;

import com.gewujie.zibian.repository.UserRepository;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Property-based persistence tests for User entity
 * Feature: phone-login-enhancement
 * 
 * Note: This test requires Spring context, so we use a hybrid approach
 * with standard JUnit tests that verify property-based behavior
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserPersistencePropertyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Feature: phone-login-enhancement, Property 2: UUID persistence round-trip
     * For any user created and saved to the database, retrieving that user 
     * should return the same UUID value
     * Validates: Requirements 1.2, 1.3
     */
    @org.junit.Test
    public void testUuidPersistenceRoundTrip() {
        // Run multiple iterations to simulate property-based testing
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setNickname("TestUser" + i);
            user.setIsVip(i % 2 == 0);
            
            // Save user (triggers @PrePersist)
            User savedUser = entityManager.persistAndFlush(user);
            String originalUuid = savedUser.getUuid();
            
            assertNotNull("UUID should be generated on persist", originalUuid);
            
            // Clear the persistence context to force a fresh retrieval
            entityManager.clear();
            
            // Retrieve user from database
            User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
            
            assertNotNull("User should be retrievable", retrievedUser);
            assertNotNull("Retrieved user should have UUID", retrievedUser.getUuid());
            assertEquals("UUID should match after round-trip", 
                        originalUuid, retrievedUser.getUuid());
            
            // Cleanup
            entityManager.remove(entityManager.merge(retrievedUser));
            entityManager.flush();
        }
    }

    /**
     * Feature: phone-login-enhancement, Property 5: Phone number retrieval
     * For any user with a phone number, retrieving that user should include 
     * the phone number in the User Entity
     * Validates: Requirements 2.3
     */
    @org.junit.Test
    public void testPhoneNumberRetrieval() {
        // Run multiple iterations with different phone numbers
        String[] testPhones = {
            "13800138000", "13900139000", "15800158000", 
            "17800178000", "18800188000", null
        };
        
        for (String phone : testPhones) {
            User user = new User();
            user.setNickname("PhoneUser_" + (phone != null ? phone : "null"));
            user.setPhone(phone);
            
            // Save user
            User savedUser = entityManager.persistAndFlush(user);
            entityManager.clear();
            
            // Retrieve user
            User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
            
            assertNotNull("User should be retrievable", retrievedUser);
            
            if (phone != null) {
                assertEquals("Phone should match after retrieval", 
                           phone, retrievedUser.getPhone());
            } else {
                assertNull("Phone should be null when not set", 
                          retrievedUser.getPhone());
            }
            
            // Cleanup
            entityManager.remove(entityManager.merge(retrievedUser));
            entityManager.flush();
        }
    }
}
