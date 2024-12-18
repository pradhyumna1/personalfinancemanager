package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public List<String> getAccessTokensByUsername(String username) {
        // Retrieve the User entity by username
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }
        User user = optionalUser.get();

        // Use the user_id to retrieve all items associated with this user
        List<Item> items = itemRepository.findByUser(user);

        // Extract and return the list of access tokens
        return items.stream()
                .map(Item::getAccessToken)
                .collect(Collectors.toList());
    }

    public List<String> getItemIdsByUsername(String username) {
        // Retrieve the User entity by username
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }
        User user = optionalUser.get();

        // Use the user_id to retrieve all items associated with this user
        List<Item> items = itemRepository.findByUser(user);

        // Extract and return the list of item IDs
        return items.stream()
                .map(Item::getItemId)
                .collect(Collectors.toList());
    }

    public Item getItemByAccessToken(String accessToken) {
        // Find the Item associated with the accessToken
        return itemRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new IllegalArgumentException("Item not found for access token: " + accessToken));
    }

    public User getUserByAccessToken(String accessToken) {
        // Retrieve the Item entity associated with the accessToken
        Item item = getItemByAccessToken(accessToken);

        // Return the associated User
        return item.getUser();
    }
}
