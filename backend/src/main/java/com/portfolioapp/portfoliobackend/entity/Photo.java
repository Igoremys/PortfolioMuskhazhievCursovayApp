package com.portfolioapp.portfoliobackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "photos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

   
    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String author;  

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserProfile userProfile;

   
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "photo_likes",
            joinColumns = @JoinColumn(name = "photo_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    @Builder.Default 
    private Set<UserProfile> likedByUsers = new HashSet<>();

   
    public void addLike(UserProfile user) {
        if (likedByUsers == null) {
            likedByUsers = new HashSet<>();
        }
        if (user != null) {
            likedByUsers.add(user);
        }
    }

    public void removeLike(UserProfile user) {
        if (likedByUsers != null && user != null) {
            likedByUsers.remove(user);
        }
    }

    public boolean isLikedBy(UserProfile user) {
        return likedByUsers != null && user != null && likedByUsers.contains(user);
    }

    public int getLikesCount() {
        return likedByUsers != null ? likedByUsers.size() : 0; 
    }
}
