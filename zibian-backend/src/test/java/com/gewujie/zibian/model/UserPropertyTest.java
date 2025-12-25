package com.gewujie.zibian.model;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Property-based tests for User entity
 * Feature: phone-login-enhancement
 */
@RunWith(JUnitQuickcheck.class)
public class UserPropertyTest {

    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );

    /**
     * Feature: phone-login-enhancement, Property 1: UUID generation uniqueness
     * For any set of newly created users, each user should have a unique UUID 
     * that is non-null and follows the standard UUID format (8-4-4-4-12 hexadecimal pattern)
     * Validates: Requirements 1.1, 1.4
     */
    @Property(trials = 100)
    public void testUuidGenerationUniqueness() {
        Set<String> uuids = new HashSet<>();
        int userCount = 50;
        
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setNickname("TestUser" + i);
            
            // Trigger @PrePersist manually for testing
            user.onCreate();
            
            String uuid = user.getUuid();
            
            // Verify UUID is not null
            assertNotNull("UUID should not be null", uuid);
            
            // Verify UUID follows standard format
            assertTrue("UUID should match standard format: " + uuid, 
                      UUID_PATTERN.matcher(uuid).matches());
            
            // Verify UUID is unique
            assertFalse("UUID should be unique, but found duplicate: " + uuid, 
                       uuids.contains(uuid));
            
            uuids.add(uuid);
        }
        
        // Verify all UUIDs are unique
        assertEquals("All UUIDs should be unique", userCount, uuids.size());
    }

    /**
     * Feature: phone-login-enhancement, Property 10: UUID format compliance
     * For any generated UUID, it should match the standard UUID format specification
     * Validates: Requirements 5.3
     */
    @Property(trials = 100)
    public void testUuidFormatCompliance() {
        User user = new User();
        user.setNickname("TestUser");
        user.onCreate();
        
        String uuid = user.getUuid();
        
        assertNotNull("UUID should not be null", uuid);
        assertEquals("UUID should be 36 characters long", 36, uuid.length());
        assertTrue("UUID should match standard format (8-4-4-4-12)", 
                  UUID_PATTERN.matcher(uuid).matches());
        
        // Verify the structure: 8-4-4-4-12 with dashes at positions 8, 13, 18, 23
        assertEquals("Dash at position 8", '-', uuid.charAt(8));
        assertEquals("Dash at position 13", '-', uuid.charAt(13));
        assertEquals("Dash at position 18", '-', uuid.charAt(18));
        assertEquals("Dash at position 23", '-', uuid.charAt(23));
    }
}
