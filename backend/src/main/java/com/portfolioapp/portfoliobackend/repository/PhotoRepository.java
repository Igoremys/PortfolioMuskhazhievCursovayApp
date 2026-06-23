package com.portfolioapp.portfoliobackend.repository;

import com.portfolioapp.portfoliobackend.entity.Photo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByAuthor(String author);
    List<Photo> findByUserProfileId(Long userId);

    default List<Photo> findByAuthorId(Long authorId) {
        return findByUserProfileId(authorId);
    }

    @Query("SELECT p FROM Photo p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Photo> searchByKeyword(@Param("keyword") String keyword);

    Optional<Photo> findByTitleAndAuthor(String title, String author);

    @EntityGraph(attributePaths = {"likedByUsers"})
    @Query("SELECT p FROM Photo p")
    List<Photo> findAllWithLikes();

    @EntityGraph(attributePaths = {"likedByUsers"})
    @Query("SELECT p FROM Photo p WHERE p.id = :id")
    Optional<Photo> findByIdWithLikes(@Param("id") Long id);

    @EntityGraph(attributePaths = {"likedByUsers"})
    @Query("SELECT p FROM Photo p WHERE p.author = :author")
    List<Photo> findByAuthorWithLikes(@Param("author") String author);

    @EntityGraph(attributePaths = {"likedByUsers"})
    List<Photo> findByUserProfileIdOrderByCreatedAtDesc(Long userId);
}
