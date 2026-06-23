package com.portfolioapp.portfoliobackend.repository;

import com.portfolioapp.portfoliobackend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;      
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    
    Optional<UserProfile> findByEmail(String email);
    boolean existsByEmail(String email);

    List<UserProfile> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email
    );
}
