package com.example.pfm_dashboard;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import com.example.pfm_dashboard.service.ItemService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemService itemService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Initialize test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Stub the userRepository to return testUser for "testuser"
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testGetAccessTokensByUsername_Success() {
        System.out.println("Starting Success Test Case...");

        // Create items with access tokens associated with test user
        Item item1 = new Item();
        item1.setAccessToken("access-token-1");
        item1.setUser(testUser);

        Item item2 = new Item();
        item2.setAccessToken("access-token-2");
        item2.setUser(testUser);

        // Mock ItemRepository to return list of items for test user
        when(itemRepository.findByUser(testUser)).thenReturn(Arrays.asList(item1, item2));

        // Call method
        List<String> accessTokens = itemService.getAccessTokensByUsername("testuser");

        // Assertions
        assertEquals(2, accessTokens.size());
        assertTrue(accessTokens.contains("access-token-1"));
        assertTrue(accessTokens.contains("access-token-2"));

        for (String accessToken : accessTokens){
            System.out.println("access_token:" + accessToken);
        }

        System.out.println("Test Case Success Completed.");
    }

    @Test
    void testGetAccessTokensByUsername_UserNotFound() {
        System.out.println("Starting User Not Found Test Case...");

        // Mock UserRepository to return empty for unknown user
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Call method and expect an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.getAccessTokensByUsername("unknownuser"));

        assertEquals("User not found with username: unknownuser", exception.getMessage());

        System.out.println("User Not Found Test Case Completed.");
    }

    @Test
    void testGetAccessTokensByUsername_NoItemsFound() {
        System.out.println("Starting No Items Found Test Case...");

        // Mock ItemRepository to return empty list for test user
        when(itemRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        // Call method
        List<String> accessTokens = itemService.getAccessTokensByUsername("testuser");

        // Assertions
        assertNotNull(accessTokens);
        assertTrue(accessTokens.isEmpty());

        System.out.println("No Items Found Test Case Completed.");
    }
}
