package com.expper.repository;

import com.expper.domain.Post;
import com.expper.domain.Tag;
import com.expper.domain.enumeration.PostStatus;
import com.expper.web.rest.dto.PostTagCountDTO;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring Data JPA repository for the Post entity.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select count(post.id) from Post post where post.status =  com.expper.domain.enumeration.PostStatus.PUBLIC")
    long countPublicPost();

    // Left join fetch query is not working with jpa page query
    // The trick is to declare a countQuery
    @Query(value = "SELECT distinct post FROM Post post " +
        "LEFT JOIN FETCH post.tags tags " +
        "WHERE post.user.id = :userId " +
        "ORDER BY post.id DESC",
        countQuery = "SELECT COUNT(post) FROM Post post WHERE post.user.id = :userId")
    Page<Post> findUserPosts(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT distinct post FROM Post post " +
        "LEFT JOIN FETCH post.tags tags " +
        "WHERE post.user.id = :userId AND post.status = :status " +
        "ORDER BY post.id DESC",
        countQuery = "SELECT COUNT(post) FROM Post post WHERE post.user.id = :userId AND post.status = :status")
    Page<Post> findUserPostsByStatus(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT post FROM Post post WHERE post.user.id = :uid AND UPPER(post.title) LIKE %:title% ORDER BY post.id DESC")
    Page<Post> searchUserPosts(Pageable pageable, @Param("uid") Long userId, @Param("title") String title);

    @Query(value = "SELECT distinct post FROM Post post " +
        "LEFT JOIN post.tags tag " +
        "WHERE post.user.login = :login " +
        "AND tag.id = :tagId " +
        "ORDER BY post.id DESC",
        countQuery = "SELECT COUNT(post) FROM Post post JOIN post.tags tag WHERE post.user.login = :login AND tag.id = :tagId")
    Page<Post> findUserPostsByTag(Pageable pageable, @Param("login") String login, @Param("tagId") Long tagId);

    @Query("SELECT post FROM Post post LEFT JOIN FETCH post.tags LEFT JOIN FETCH post.user WHERE post.id = ?1 AND post.user.id = ?2")
    Post findUserPost(Long id, Long userId);

    @Query("SELECT post FROM Post post LEFT JOIN FETCH post.tags LEFT JOIN FETCH post.user WHERE post.id = ?1")
    Post findPostWithTags(Long id);

    @Query("SELECT new com.expper.web.rest.dto.PostTagCountDTO(count(post.id) AS c, tag) " +
        "FROM Post post " +
        "JOIN post.tags AS tag " +
        "WHERE post.user.id = :uid " +
        "GROUP BY tag.id " +
        "ORDER BY c DESC")
    List<PostTagCountDTO> countPostsByTags(@Param("uid") Long userId);


    @Query("SELECT COUNT(post.id) AS c, tag FROM Post post JOIN post.tags AS tag WHERE post.user.id = ?1 GROUP BY tag.id ORDER BY c DESC")
    List<Object[]> countUserPostsByTags(Long userId);

    @Query("SELECT COUNT(post.id) FROM Post post WHERE post.user.id = ?1")
    Long countUserPosts(Long userId);

    @Query("select p from Post p where p.status = :status")
    Page<Post> findPublicPosts(Pageable pageable, @Param("status") PostStatus status);

    Post findByStatusAndId(PostStatus postStatus, Long id);

    @Query("select distinct post from Post post left join fetch post.tags left join fetch post.user where post.id in (?1) order by post.id desc")
    List<Post> findByIdIn(Collection<Long> ids);

    @Modifying
    @Query(value = "UPDATE post SET hits=?4, replies=?5, votes_down=?3, votes_up=?2 WHERE id=?1", nativeQuery = true)
    void updateCounting(long postId, int votesUp, int votesDown, int hits, int replies);
}
