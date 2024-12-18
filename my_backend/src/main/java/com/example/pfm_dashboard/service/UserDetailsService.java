package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.model.UserDetails;
import com.example.pfm_dashboard.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService {

    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public UserDetailsService(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    public void saveUserDetails(UserDetails userDetails) {
        userDetailsRepository.save(userDetails);
    }

    public UserDetails findByUserId(Long userId) {
        return userDetailsRepository.findByUserId(userId);
    }
}
