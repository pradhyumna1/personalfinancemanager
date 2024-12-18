package com.example.pfm_dashboard.controller;

import com.example.pfm_dashboard.model.LoginRequest;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.model.UserDetails;
import com.example.pfm_dashboard.service.UserService;
import com.example.pfm_dashboard.service.UserDetailsService;
import com.example.pfm_dashboard.service.ItemService;
import com.plaid.client.request.PlaidApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final PlaidApi plaidClient;
    private final ItemService itemService;

    @Autowired
    public UserController(UserService userService, UserDetailsService userDetailsService, PlaidApi plaidClient, ItemService itemService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.plaidClient = plaidClient;
        this.itemService = itemService;
    }

    // Register a new user with additional user details
    @PostMapping("/registerUser")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User savedUser = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    @PostMapping("/registerUserDetails")
    public ResponseEntity<?> registerUserDetails(@RequestBody Map<String, Object> userDetailsPayload) {
        try {
            // Extract userId from payload
            Long userId = Long.valueOf(userDetailsPayload.get("userId").toString());
            
            // Extract other fields
            String firstName = userDetailsPayload.get("firstName").toString();
            String lastName = userDetailsPayload.get("lastName").toString();
            String address = userDetailsPayload.get("address").toString();
            String phoneNumber = userDetailsPayload.get("phoneNumber").toString();

            // Call service to handle business logic
            UserDetails userDetails = userService.registerUserDetails(userId, firstName, lastName, address, phoneNumber);

            return new ResponseEntity<>(userDetails, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



   @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            String usernameOrEmail = loginRequest.getUsernameOrEmail();
            String password = loginRequest.getPassword();

            if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
                throw new RuntimeException("Username or email must be provided.");
            }

            // Authenticate user
            User user = userService.loginUser(usernameOrEmail, password);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }


    // Find or create user by username
    @GetMapping("/findOrCreate/{username}")
    public ResponseEntity<User> findOrCreateUser(@PathVariable String username) {
        User user = userService.findOrCreateUser(username);
        return ResponseEntity.ok(user);
    }

    // Retrieve a user by username
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        try {
            User user = userService.findUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Retrieve user access tokens by username
    @GetMapping("/{username}/accessToken")
    public ResponseEntity<List<String>> getAccessToken(@PathVariable String username) {
        try {
            List<String> accessTokens = itemService.getAccessTokensByUsername(username);
            return ResponseEntity.ok(accessTokens);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.singletonList(e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // Retrieve user details by username
    @GetMapping("/{username}/details")
    public ResponseEntity<UserDetails> getUserDetails(@PathVariable String username) {
        try {
            User user = userService.findUserByUsername(username);
            UserDetails userDetails = userDetailsService.findByUserId(user.getId());
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Update user details
    @PutMapping("/{username}/details")
    public ResponseEntity<?> updateUserDetails(@PathVariable String username, @RequestBody UserDetails updatedDetails) {
        try {
            User user = userService.findUserByUsername(username);
            UserDetails existingDetails = userDetailsService.findByUserId(user.getId());

            if (existingDetails == null) {
                return new ResponseEntity<>("User details not found", HttpStatus.NOT_FOUND);
            }

            // Update details
            existingDetails.setFirstName(updatedDetails.getFirstName());
            existingDetails.setLastName(updatedDetails.getLastName());
            existingDetails.setAddress(updatedDetails.getAddress());
            existingDetails.setPhoneNumber(updatedDetails.getPhoneNumber());
            userDetailsService.saveUserDetails(existingDetails);

            return ResponseEntity.ok(existingDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating user details: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
