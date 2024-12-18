package com.example.pfm_dashboard.repository;

import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUser(User user);

    Optional<Item> findByItemId(String itemId);
    
    Optional<Item> findByAccessToken(String accessToken);

}
