package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.model.UserDetails;
import com.example.pfm_dashboard.repository.UserRepository;
import com.example.pfm_dashboard.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                       UserDetailsRepository userDetailsRepository, 
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register a new user
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()) || userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("User with this email or username already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Register user details
    public UserDetails registerUserDetails(Long userId, String firstName, String lastName, String address, String phoneNumber) throws Exception {
        // Fetch the User entity by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Create and populate UserDetails
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName(firstName);
        userDetails.setLastName(lastName);
        userDetails.setAddress(address);
        userDetails.setPhoneNumber(phoneNumber);
        userDetails.setUser(user); // Set the User entity

        // Save UserDetails to the database
        return userDetailsRepository.save(userDetails);
    }

    // Login method to validate user credentials
    public User loginUser(String usernameOrEmail, String password) throws Exception {
        Optional<User> userOptional;
    
        // Check if `usernameOrEmail` matches either a username or an email
        if (usernameOrEmail.contains("@")) { // Likely an email
            userOptional = userRepository.findByEmail(usernameOrEmail);
        } else { // Likely a username
            userOptional = userRepository.findByUsername(usernameOrEmail);
        }
    
        User user = userOptional
                .orElseThrow(() -> new RuntimeException("User not found with provided credentials."));
    
        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid username/email or password.");
        }
    
        return user;
    }    

    // Find or create a user by username
    public User findOrCreateUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            return userRepository.save(newUser);
        });
    }

    // Save a user to the database
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Find a user by username
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
}
